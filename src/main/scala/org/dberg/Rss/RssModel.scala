package org.dberg.Rss

import java.util.{Date}
import scala.xml._
import java.text.SimpleDateFormat
import java.util.Locale
import scala.concurrent._



trait RssFeed {
  val title: String
  val desc: String
  val link: String

  def items: Future[Set[RssItem]]

  //def extract(body: String): Seq[RssItem]
}

case class FeedUrl(url: String, id: Long)

//case class AtomRssFeed(title:String, link:String, desc:String, items:Seq[RssItem]) extends RssFeed {
//
//}

case class XmlRssFeed(id: Long, title:String, link:String, desc:String, language:String) extends RssFeed {

  val dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH)

  def items = RssDAO.getItems(this.id)



}

case class RssItem(title:String, link:String, desc:String, guid:String, date:Option[Date] = None ) {
  override def toString = "title: " + title + "\n" + "date: " + date.getOrElse("") + "\n" + "link : " + link + "\n" + "guid: " + guid + "\n"
}