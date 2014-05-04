package org.dberg.Rss

import akka.actor.Actor
import org.slf4j.{Logger, LoggerFactory}
import org.dberg.Rss.Actors.Dispatchers._
import java.text.SimpleDateFormat
import java.util.Locale
import scala.util._
import scala.concurrent.ExecutionContext.Implicits.global
import org.dberg.Rss.Util.RssHelpers




class URLParser extends Actor with RssHelpers {

  def guid: String = Random.alphanumeric.take(10).mkString

  def parse(f: FeedUrl, body: xml.Elem) = {

    val feed = RssDAO.getFeed(f,body)

    feed.onComplete {
      case Success(r: RssFeed) => {
        logger.info("Successfully got Feed " + r)
        r.items.onComplete {
          case Success(s) => s.map(f => logger.info(f.toString))
          case Failure(y) => logger.error("Failed to fetch items for Rss feed " + y)
          case _ => logger.error("No clue what happened")
        }
      }
      case Failure(y) => logger.error("Error getting feed for " + f + " error: " + y.getMessage )
    }


    //feed.extract(body).map(i =>  ! (f,i) )

    // for { i <- 1 to 2 } yield {Thread.sleep(1000); rssUpdater ! (f,RssItem("Some Title",f.url,"some description",dateFormatter.parse("2013-10-02"),g)) }


  }

  def receive = {
    case (url: FeedUrl,body: xml.Elem) =>  parse(url,body)
    case x => logger.error("Unknown type passed to URLparser: " + x + " from sender " + sender)
  }

}
