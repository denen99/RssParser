package io.socialstream.Rss

import akka.actor.Actor
import org.slf4j.{Logger, LoggerFactory}
import io.socialstream.Rss.Actors.Dispatchers._
import java.text.SimpleDateFormat
import java.util.Locale
import scala.util._
import scala.concurrent.ExecutionContext.Implicits.global
import io.socialstream.Rss.Util.RssHelpers




class URLParser extends Actor with RssHelpers {

  def guid: String = Random.alphanumeric.take(10).mkString

  def parse(f: FeedUrl, body: xml.Elem) = {

    val feed = RssDAO.getFeed(f,body)

    feed.onComplete {
      case Success(r: RssFeed) => {
        logger.info("Successfully got Feed " + r)
        r.items.onComplete {
          case Success(s) => s.map(f => println(f))
          case Failure(y) => throw new Exception("Failed to fetch items for Rss feed " + y)
          case _ => throw new Exception("No clue what happened")
        }
      }
      case Failure(y) => throw new Exception("Error getting feed for " + f )
    }


    //feed.extract(body).map(i =>  ! (f,i) )

    // for { i <- 1 to 2 } yield {Thread.sleep(1000); rssUpdater ! (f,RssItem("Some Title",f.url,"some description",dateFormatter.parse("2013-10-02"),g)) }


  }

  def receive = {
    case (url: FeedUrl,body: xml.Elem) =>  parse(url,body)
    case x => throw new Exception("Unknown type passed to URLparser: " + x + " from sender " + sender)
  }

}
