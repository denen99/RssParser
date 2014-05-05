package org.dberg.Rss

import akka.actor.Actor
import scala.concurrent.Future
import dispatch._
import scala.concurrent.ExecutionContext.Implicits.global
import org.slf4j.{Logger, LoggerFactory}
import org.dberg.Rss.Actors.Dispatchers._
import scala.util.{Success,Failure}
import com.ning.http.client.Response
import scala.xml.Elem


class URLFetcher extends Actor {

  val logger =  LoggerFactory.getLogger(getClass)

  //---------------------------
  // Fetch a URL
  //---------------------------
  def fetch(feedUrl: FeedUrl): Unit ={
    val resp: Either[Throwable,Elem] = Http.configure(_ setFollowRedirects true)(url(feedUrl.url) OK as.xml.Elem).either()
    resp match {
      case Right(x) => urlParser ! (feedUrl,x)  // send body
      case Left(x) => logger.error("Failed to retreive URL with response : " + x.getMessage )
    }
  }

  def receive = {
    case url: FeedUrl => { logger.debug("Parsing URL " + url + "\n\n"); fetch(url) }
    case _ => logger.error("Unknown type passed to URLFetcher")
  }

}
