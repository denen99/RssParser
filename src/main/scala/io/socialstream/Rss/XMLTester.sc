import java.util.{Locale, Date}
import scala.xml.{XML, Elem, Node}
import java.text.SimpleDateFormat

val dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
val d = "Sun, 02 Mar 2014 20:00:46 +0000"
val f = dateFormatter.parse(d)

f.getTime / 1000






