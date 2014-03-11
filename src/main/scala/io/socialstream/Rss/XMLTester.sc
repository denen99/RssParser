import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


def rget(s: String): Future[Option[String]] = Future { Some( s + " Future")}

def rnext(): Future[String] = Future { "next" }



rget("test").flatMap { r => r.fold(rnext())(a => Future {a} )  }.map( y => println("Y IS " + y) )








