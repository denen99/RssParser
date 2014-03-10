package io.socialstream.Rss

import akka.actor.Actor
import scala.concurrent.Future
import dispatch._
import scala.concurrent.ExecutionContext.Implicits.global
import org.slf4j.{Logger, LoggerFactory}
import io.socialstream.Rss.Actors.Dispatchers._
import scala.util.{Success,Failure}


class URLFetcher extends Actor {

  val logger =  LoggerFactory.getLogger(getClass)

  //---------------------------
  // Fetch a URL
  //---------------------------
  def fetch(feedUrl: FeedUrl): Unit ={
    val resp = Http(url(feedUrl.url) OK as.xml.Elem)
    resp.onComplete {
      case Success(x) => urlParser ! (feedUrl,x)  // send body
      case Failure(x) => logger.error("Failed to retreive URL with response : " + x )
    }
  }

  def receive = {
    case url: FeedUrl => { logger.info("Parsing URL " + url + "\n\n"); fetch(url) }
    case _ => logger.info("Unknown type passed to URLFetcher")
  }

}
