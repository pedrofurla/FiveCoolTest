package database

import database.scala.DummyDB2
import database.scala.Types._
import org.specs2.mutable.Specification

import scalaz.\/-

/**
 * @author Arnaud Tanguy <arnaud@fivecool.net>
 *         Date: 22/05/2012
 *
 */
class DummyDB2Spec extends Specification {
  val db = new DummyDB2(0) // NOTE zero chance of

  "datastore time ranges" should {
    "increment fail for invalid hour" in {
      db.incCount(-1,0).isLeft &&
      db.incCount(24,0).isLeft &&
      db.incCount(1,-1).isLeft &&
      db.incCount(1,60).isLeft
    }

    "retrieval fail for invalid hour" in {
      db.timeCount(-1,0).isLeft &&
      db.timeCount(24,0).isLeft &&
      db.timeCount(1,-1).isLeft &&
      db.timeCount(1,60).isLeft
    }
  }

  "datastore increment operation " should {
    "increment count for a time" in {
      db.incCount((0,0))
      db.timeCount(0,0) mustEqual \/-(1L)
    }

    "not increment existing other times" in {
      db.incCount((0,1))
      db.timeCount(0,3) mustEqual \/-(0L)
    }

  }

  // TODO we could have a another db that could fail and test it against Application.retry

}
