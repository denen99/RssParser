package org.dberg.Rss.Util

import java.util.{Locale, Date}
import scala.concurrent.duration._
import java.text.SimpleDateFormat
import org.slf4j.LoggerFactory
import com.redis.RedisClient
import akka.util.Timeout


trait RssHelpers {

  implicit val akkaSystem = akka.actor.ActorSystem("RssParser")

  def dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

  val logger =  LoggerFactory.getLogger(getClass)
  val redis = RedisClient("localhost",6379)
  implicit val timeout = Timeout(5 seconds)

  //---------------------------------------
  // Function to return a date or None if
  // Empty since its not required
  //--------------------------------------
  def getDate(s: String): Option[Date] = {
    if (s.isEmpty)
      None
    else {
      Some(dateFormatter.parse(s))
    }
  }

  //---------------------------------------
  // Function to return a guid or link if
  // Empty since we need something unique
  //--------------------------------------
  def getGuid(guid: String, link: String): String = {
    if (guid.isEmpty && link.isEmpty) throw new Exception("Can not have guid and link be empty, we need SOMEthing unique !")

    if (guid.isEmpty)
      link
    else
      guid
  }



}
