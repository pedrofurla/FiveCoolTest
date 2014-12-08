package database.scala

object DummyDB2 extends DummyDB2(1) // near 10% probability of failure

/**
 * DummyDBScala is the Scala implementation of our DataStore
 * @author Pedro
 *         Date: 03/12/2014
 *
 */
class DummyDB2(failProbability: Int) {

  import database.StorageException

  import scalaz._
  import Scalaz._
  import collection.immutable.{ HashMap => IMap }
  import collection.mutable.{ HashMap => MMap }

  import Types._

  private def mayFail(): Unit =
    if (scala.math.random * (10) < failProbability)
      throw new StorageException("Simulated store failure")

  /** Time in TimeCount is used as lock in synchronizing the map */
  private val timeDb: IMap[Int, HourMap] =
    IMap apply
    (0 to 23 map { hour =>
      val counter = 0 to 59 map { min =>
        val time = (hour, min)
        (time, (time,0L))
      };
      (hour, MMap(counter:_*))
    } : _* )

  private def getTime(t: Time): ErrorOr[(Time, Long, HourMap)] = {
    (for {
      hourMap <- timeDb get t._1
      (time, count) <- hourMap get t
      _ = mayFail()
    } yield (time, count, hourMap)) \/> "wrong time frame / datetime not valid"
  }
  //private def getTime(t:Long): ErrorOr[(Time, Long, HourMap)] =
  //  getTime(mkTime(t))

  def incCount(t: Time): ErrorOr[Unit] = // TODO Should we use Unit here and don't check the ranges?
    getTime(t) map { case (time, count, hourMap:HourMap) =>
      time.synchronized { hourMap(time) = time -> (count + 1) } // it's VERY important to keep the time reference here
      ()
    }

  def timeCount(t:Time): ErrorOr[Long] = getTime(t) map { _._2 }

  def hourCount(h: Int): ErrorOr[Long] = {
    mayFail()
    (for {hourMap <- timeDb get h}
      yield hourMap.values.toList foldMap { case (_, c) => c}) \/> s"Invalid time range (h:m): t"
  }

}
