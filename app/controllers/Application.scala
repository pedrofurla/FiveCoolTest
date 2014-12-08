package controllers

import database.StorageException
import play.api.Logger
import play.api.mvc._

object Application extends Controller {

  import scalaz._
  import Scalaz._
  import play.api.libs.json._
  import play.api.libs.functional.syntax._
  import database.scala._
  import database.scala.Types._

  // Attention:  play's default threadPool
  import play.api.libs.concurrent.Execution.Implicits._

  def toDisjunction[A]: JsResult[A] => ErrorOr[A] = js => js.asOpt \/> "the Json is malformed"

  val hitJson: (JsValue) => ErrorOr[(String, String)] = {
    ((__ \ 'userId).read[String] and
     (__ \ 'action).read[String]
    ).tupled.reads(_) } andThen toDisjunction

  import scala.concurrent.Future

  def hit = Action(parse.json) { r =>
    val res = hitJson(r.body) map { case (user, action) =>
      retry(DummyDB2 incCount now) map { _ =>
        // TODO COUNT USER
        Ok("stat submitted") }
    } valueOr { e => Future { BadRequest(e) } }
    Async { res }
  }

  def insightUser(userId:String) = Action { NotImplemented("TODO") }

  def insightHit(resolution: ErrorOr[Resolution], datetime:Timestamp) = Action { r =>
    val res = retry{
      for{
        res <- resolution leftMap { e => BadRequest(e) }
        count <-
          (if (res == Minute) DummyDB2 hourCount mkTime(datetime)._1
          else DummyDB2 timeCount mkTime(datetime) ) leftMap { e => NotAcceptable(e) }
      } yield Ok(insightHitJson(res,datetime,count))
    }

    Async { res map { _.fold(identity, identity) } }
  }

  import scala.concurrent.duration.SECONDS
  import play.api.libs.concurrent.Promise

  def retry[A](u: => A, attempt: Int = 0):Future[A] =
    Future { u } recoverWith { // poor man's futuristic pseudo-flatMap
      case e:StorageException =>
        Logger.error(s"StorageException: ${e.getMessage}")
        if (attempt<5) retry(u, attempt+1)
        else Promise.timeout(retry(u), 5, SECONDS) flatMap { identity }
    }


}