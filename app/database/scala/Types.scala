package database.scala

import play.api.libs.json.Json

import scala.collection.mutable.HashMap


object Types {

  import collection.mutable.{ HashMap => MMap }
  import org.joda.time.LocalTime
  import scalaz._
  import Scalaz._

  type Time = (Int, Int) // (Hour, Minutes)
  type TimeCount = (Time, Long)
  type HourMap = MMap[Time, TimeCount] // A map for a particular hour
  type Timestamp = Long

  type Error = String
  type ErrorOr[A] = \/[Error,A]

  trait Resolution { val name:String }
  case object Minute extends Resolution { val name = "minutely" }
  case object Hour extends Resolution { val name = "hour" }
  object Resolution {
    implicit def str2Resolution(s:String): ErrorOr[Resolution] =
      if (s == Minute.name)
        \/-(Minute)
      else if (s == Hour.name)
        \/-(Hour)
      else -\/("time resolution incorrect")
  }

  def mkTime(t:Long): Time = {
    val t2 = new LocalTime(t)
    (t2.getHourOfDay, t2.getMinuteOfHour)
  }

  def now:Time = {
    val ld = new LocalTime()
    (ld.getHourOfDay, ld.getMinuteOfHour)
  }

  def insightHitJson(res:Resolution, datetime:Timestamp, count: Long) =
    Json.obj(
      "timeresolution" -> res.name,
      "datetime"       -> datetime,
      "value"          -> count)

}
