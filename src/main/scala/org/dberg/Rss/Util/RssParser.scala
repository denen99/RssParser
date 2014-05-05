package org.dberg.Rss.Util

import scala.xml.Elem
import org.dberg.Rss.{RssFeed, FeedUrl, RssItem}
import org.dberg.Rss.RssDAO
import org.jsoup._
import dispatch._
import scala.concurrent.ExecutionContext.Implicits.global



class RssParser extends RssHelpers {


  /*------------------------------------------------------------
   Parse the RSS Items from an XML document and return a Seq of
    RssItem objects
   ------------------------------------------------------------*/
   protected def parseItems(xml: Elem): Seq[RssItem] =  {

    val items = for (channel <- xml \ "channel") yield {
      for (item <- channel \ "item") yield {
        RssItem(
          (item \ "title").filter(_.prefix != "media").text,
          (item \ "link").text,
          (item \ "description").text,
          getGuid((item \ "guid").text,(item \ "link").text),
          getDate((item \ "pubDate").text)
        )
      }
    }
    items.flatten
  }


  def parseHtml(link: String): Future[String] = {
    val resp = Http.configure(_ setFollowRedirects true)(url(link) OK as.String)

    resp.map { r =>
      val p = Jsoup.parse(r)
      p.body().text()
    }
  }

  /*------------------------------------------------
    Takes XML body and RSS Feed Object
   --------------------------------------------------*/
   protected def parseFeedXML(f: FeedUrl,  xml: Elem): RssFeed = {
    logger.debug("Parsing XML Feed : " + f.url)

    val title = ( xml \\ "channel" \ "title").text
    val desc  = ( xml \\ "channel" \ "description").text
    val lang  = ( xml \\ "channel" \ "language").text

    redis.hset("feed:" + f.id, "id",f.id.toString)
    redis.hset("feed:" + f.id, "title", title)
    redis.hset("feed:" + f.id, "desc", desc)
    redis.hset("feed:" + f.id, "link", f.url)
    redis.hset("feed:" + f.id, "type", "rss")
    redis.hset("feed:" + f.id, "lang", lang)

    parseItems(xml).map(i => RssDAO.saveItem(f.id,i) )

    RssDAO.buildRssFromMap(Map("id" -> f.id.toString , "title" -> title, "desc" -> desc, "link" -> f.url, "lang" -> lang))
  }

  /*---------------------------------------------
   Return the RSS Object of a given feedURL
  ------------------------------------------------ */
   def parseFeed(f: FeedUrl,  body: xml.Elem): RssFeed = {
    if ( (body \ "channel").length == 0)
      throw new Exception("Not an XML FEED!")
    else
      parseFeedXML(f,body)
  }

}
