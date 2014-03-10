package io.socialstream.Rss

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Success, Failure}
import scala.xml.{XML, Elem}
import io.socialstream.Rss.Util.{RssHelpers,RssParser}
import com.textteaser.summarizer._
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import com.google.inject.Guice
import io.socialstream.Rss.Util._
//import org.slf4j._
import com.textteaser.summarizer.Summarizer




object RssDAO extends RssHelpers {


  val parser = new RssParser

  val config = new Config
  val guice = new ScalaInjector(Guice.createInjector(new GuiceModule(config, true)))
  val summarizer = guice.instance[Summarizer]


  /*---------------------------------------------
    Return the next ID for autoIncrement purposes
  ------------------------------------------------ */
  protected def nextId(t: String): Future[Long] = t match {
    case "feed" => {logger.info("Incrementing feed id") ; redis.incr("feed:id") }
    case "item" => redis.incr("item:id")
    case x => throw new Exception("Unknown increment key passed : " + x)
  }


  /*------------------------------------------------------
   Contruct an RSSFeed object from a Map that likely came
   from the hget in Redis
   -------------------------------------------------------*/
   def buildRssFromMap(m: Map[String,String]): RssFeed =
    XmlRssFeed(m("id").toLong,m("title"),m("link"),m("desc"),m("lang"))



  /*-----------------------------------------------------
  // Return ID of Feed in key feed:URL
  // if it doesnt exist, set it and return the ID
  -------------------------------------------------------*/
  protected def getFeedId(url: String): Long = {
   val key = "feed:" + url
   val p = Promise[String]

   logger.info("Testing key " + key)

   redis.get(key).onComplete {
         case Success(Some(i: String)) =>  {
           p.success(i)
         }
         case Success(None) => nextId("feed").onComplete {
           case Success(j: Long) => {
             redis.set(key,j.toString);
             p.success(j.toString);
           }
           case Failure(x)  => { logger.info("nextid failed"); p.failure(throw new Exception("Unable to increment ID for key " + key + " " + x)) }
         }
         case Failure(x) => logger.info("Failed to get key " + key + " for feed : " + x)
    }


    Await.result(p.future,500 milliseconds)

    p.future.value match {
      case Some(Success(x: String)) => { logger.info("Returning Long : " + x) ; x.toLong }
      case Some(Failure(y)) => throw new Exception("Unable to get feed id for URL " + url + " : " + y)
      case x => { logger.info("We bombed out here : " + x ); throw new Exception("Unable to get ID from Redis !!")  }
    }

  }

  ///////////////////////////////////////////////////
  //   PUBLIC CLASS METHODS
  ///////////////////////////////////////////////////

  /*-------------------------------------------
   Get the list of FeedURLs that we need to parse
  --------------------------------------------*/
  def getFeedUrls: Future[List[FeedUrl]] = {
    for {
      feed <- redis.smembers("feeds")
    } yield
      feed.map(v => {
        //val id = getFeedId(v)
        FeedUrl(v,getFeedId(v))
      }).toList
  }

  /*--------------------------------------------------
   Return an instance of the RssFeed Object from the given
   FeedURL.  If its not in Redis, parse the Body and
   create the necessary keys in redis for this Feed
   ---------------------------------------------------*/
  def getFeed(feed: FeedUrl, body: xml.Elem): Future[RssFeed] = {
    val p = Promise[RssFeed]()
    future {
      p.success(parser.parseFeed(feed,body))
    }
    p.future
  }

  def saveItem(feedId: Long, item: RssItem): Unit = {
    val key = "feed:" + feedId + ":items"
    val itemKey = feedId + ":" + item.guid


    logger.debug("About to save items for guid: " + item.guid + " and link : " + item.link )
    redis.sismember(key,item.guid).onComplete {
      case Success(b: Boolean) if b == true => { logger.debug("Skipping existing guid " + item.guid + " for feedid " + feedId) }
      case Success(b: Boolean) if b == false => {

       parser.parseHtml(item.link).map { text =>
         val summary = summarizer.summarize(text,item.title,"","","")
         logger.info("Summary is " + summary.results)
         summary.results.map{ r =>
          logger.info("**** RESULT for title: " + item.title + " Summary: "  + r.sentence)

          logger.debug("Saving guid " + item.guid + " to key " + key)
          redis.sadd(key,item.guid)
          logger.debug("Setting itemkey " + itemKey)
          redis.hset(itemKey,"title",item.title)
          redis.hset(itemKey,"desc",item.desc)
          redis.hset(itemKey,"link",item.link)
          redis.hset(itemKey,"guid",item.guid)
          redis.hset(itemKey,"date",dateFormatter.format(item.date.getOrElse("")))
         }
       }
      }
      case Failure(b) => throw new Exception("Invalid return type for sismember")
    }
  }

  def getItems(feedId: Long): Future[Seq[RssItem]] = {
    val p = Promise[Future[Seq[RssItem]]]()
    //val dateFormatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);


    def getHashAcc(members: Set[String], items: Seq[Future[RssItem]] = Nil): Seq[Future[RssItem]] = {
      if (members.isEmpty)   {
        p.success(Future.sequence(items))
        items
      }
      else
        getHashAcc(members.tail,items :+ getHash(feedId + ":" + members.head))
    }

    def getHash(key: String ): Future[RssItem] = {
      logger.debug("Trying to HGETALL RssItem from Redis with key " + key)
      val p = Promise[RssItem]
      redis.hgetall(key).onComplete {
        case Failure(x) => throw new Exception("Unable to hgetAll for key " + key )
        case Success(h: Map[String,String]) => p.success(RssItem(h("title"),h("link"),h("desc"),getGuid(h("guid"),h("link")), getDate(h.getOrElse("date",""))))
        case x => throw new Exception("Unable to match hgetall on " + x)
      }

      p.future
    }

    val key = "feed:" + feedId + ":items"
    redis.smembers(key).onComplete {
      case Success(guids: Set[String]) => getHashAcc(guids)
      case Failure(x) => throw new Exception("smembers bombed out")
    }

    p.future.flatMap(x => x)

  }



}
