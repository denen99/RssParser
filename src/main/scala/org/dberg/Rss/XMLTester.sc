import org.joda.time.DateTime
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


val d = new DateTime()
d.getMillis / 1000

val d2 = d.plusHours(48).getMillis / 1000








