import io.socialstream.Rss._

object RssTest extends App {
  def doit = {
    val f = FeedUrl("http://www.google.com")
    RssDAO.test(f)
  }
}
RssTest.doit



