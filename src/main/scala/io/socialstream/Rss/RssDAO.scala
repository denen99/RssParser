package io.socialstream.Rss

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Success, Failure}
import com.textteaser.summarizer._
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import com.google.inject.Guice
import io.socialstream.Rss.Util._
import com.textteaser.summarizer.Summarizer




object RssDAO extends RssHelpers {

  val parser = new RssParser
  val config = new Config
  val guice = new ScalaInjector(Guice.createInjector(new GuiceModule(config, true)))
  val summarizer = guice.instance[Summarizer]


  /*---------------------------------------------
    Return the next ID for autoIncrement purposes
  ------------------------------------------------ */
  protected def nextId(t: String, key: String): Future[String] = t match {
    case "feed" => {
      logger.info("Incrementing feed id") ;
      val i = redis.incr("feed:id")
      i.map(n => redis.set(key,n.toString))
      i.map(x => x.toString)
    }
    case "item" => {
      logger.info("Incrementing item id") ;
      val i = redis.incr("item:id")
      i.map(n => redis.set(key,n.toString))
      i.map(x => x.toString)
    }
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
  protected def getFeedId(url: String): Future[String] = {
   val key = "feed:" + url

    redis.get(key).flatMap { r =>
     r.fold(nextId("feed",key))(a => Future {a}  )
   }

  }


  ///////////////////////////////////////////////////
  //   PUBLIC CLASS METHODS
  ///////////////////////////////////////////////////

  /*-------------------------------------------
   Get the list of FeedURLs that we need to parse
  --------------------------------------------*/
  def getFeedUrls: Future[List[FeedUrl]] = {
     redis.smembers("feeds").flatMap { s =>
      Future.sequence(s.map{ elem =>
        getFeedId(elem: String).map{ i =>
          FeedUrl(elem,i.toLong)
        }
      }.toList)
     }
  }


  /*--------------------------------------------------
   Return an instance of the RssFeed Object from the given
   FeedURL.  If its not in Redis, parse the Body and
   create the necessary keys in redis for this Feed
   ---------------------------------------------------*/
  def getFeed(feed: FeedUrl, body: xml.Elem): Future[RssFeed] =
    Future { parser.parseFeed(feed,body) }


  /** ***************************************
    * Save an Item to Redis
    * @param feedId
    * @param item
    */
  def saveItem(feedId: Long, item: RssItem): Unit = {
    val key = "feed:" + feedId + ":items"
    val itemKey = "item:" + feedId + ":" + item.guid
    val summaryKey = "summary:" + feedId + ":" + item.guid

    redis.sismember(key,item.guid).map {
       case(b: Boolean) if b == false => {
         logger.debug("Saving guid " + item.guid + " to key " + key)
         redis.sadd(key,item.guid)
         logger.debug("Saving item to hash  " + itemKey)
         redis.hset(itemKey,"title",item.title)
         redis.hset(itemKey,"desc",item.desc)
         redis.hset(itemKey,"link",item.link)
         redis.hset(itemKey,"guid",item.guid)
         redis.hset(itemKey,"date",dateFormatter.format(item.date.getOrElse("")))
         parser.parseHtml(item.link).map { text =>
           val summary = summarizer.summarize(text,item.title,"","","")
           summary.results.map { r =>
             redis.sadd(summaryKey,r.sentence)
           }
         }
      }
      case _ =>  logger.debug("Skipping existing guid " + item.guid + " for feedid " + feedId)
    }
  }


  /** *****************************************************
  * Returns a list of items from Redis.  Its a little tricky
  * B/c we need to first get the GUIDs from a set, then map
  *  over that set and then do an HGETALL on that key to
  *  return the hash.  Future.sequence to the rescue !
  */
  def getItems(feedId: Long): Future[Set[RssItem]] = {

    val key = "feed:" + feedId + ":items"

    def getHash(guid: String) =
      redis.hgetall("item:" + feedId + ":" + guid)

    def buildRssItem(guid: String): Future[RssItem] = {
      getHash(guid).map( h =>
        RssItem(h("title"),h("link"),h("desc"),getGuid(h("guid"),h("link")),getDate(h.getOrElse("date","")) ))
    }

    redis.smembers(key).flatMap { s: Set[String] =>
      Future.sequence(s.map { guid =>
        buildRssItem(guid)
      })
    }

  }



}
