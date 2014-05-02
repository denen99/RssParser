package org.dberg.Rss

import akka.actor.Actor
import java.text.SimpleDateFormat
import java.util.Locale
import org.slf4j.{Logger, LoggerFactory}


class RSSUpdater extends Actor  {

  val dateFormatter = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH)
  val logger =  LoggerFactory.getLogger(getClass)


  def receive = {
    case (feedId: Long,item:RssItem) => {logger.info("Calling RssDao.save"); RssDAO.saveItem(feedId,item) }
    case i => { logger.info("FAILURE saving " + i); throw new Exception("Unknown type in RSSUpdater " + i) }
  }
}
