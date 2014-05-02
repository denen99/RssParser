package org.dberg.Rss

import scala.concurrent.ExecutionContext.Implicits.global
import java.text.SimpleDateFormat
import java.util.{Locale, Date}
import scala.util.{Success,Failure}
import org.slf4j.{Logger, LoggerFactory}
import org.dberg.Rss.Actors.Dispatchers
import org.dberg.Rss.Actors.Dispatchers._

object Main extends App {

 // val dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
 val dateFormatter = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH)

 val logger =  LoggerFactory.getLogger(getClass)

 Dispatchers.startActorSystem

 def doIt =  {
   RssDAO.getFeedUrls.onComplete {
     case Success(e) => e.foreach(url => {logger.debug("Sending url to URLFetcher: " + url);  urlFetcher ! url})
     case Failure(e) => {logger.info("FAILED"); logger.info("Unable to get URLS from redis " + e) }
   }
 }


  //spawn {
    while (true) {
      Thread.sleep(10000);
      doIt
    }
  //}

}
