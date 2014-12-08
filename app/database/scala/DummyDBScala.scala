package database.scala


/**
 * DummyDBScala is the Scala implementation of our DataStore
 * @author Arnaud Tanguy <arnaud@fivecool.net>
 *         Date: 22/05/2012
 *
 */
object DummyDBScala { // SMELL: this entire class seems bogus.

  import database.StorageException

  private var db = Map[String, Any]() // SMELL: this data structure doesn't

  /**
   * Put the string value of a key
   * @param key
   * @param value
   * @throws database.StorageException
   */
  @throws(classOf[StorageException])  // don't throw exceptions
  def put(key: String, value: String): Unit = synchronized { // SMELL really needs to have a random number gen. in synch. block?
    val test = scala.math.random * (15 - 0)
    if (test < 2)
      throw new StorageException("Simulated store failure " + value)
    db += (key -> value)
  }

  /**
   * Get the value of a key
   * @param key
   * @throws database.StorageException
   * @return
   */
  @throws(classOf[StorageException])
  def get(key: String): Option[String] = synchronized { // BTW, make db volatile and don't synch here
    val test = scala.math.random * (15 - 0)
    if (test < 1)                                         // SMELL: or even throw the exception from the synch. block
      throw new StorageException("Simulated read failure " + key)
    db.get(key).map(res => Some(db.get(key).get.toString)).getOrElse(None)
  }

  /**
   * Increment the integer value of a key by the given amount
   * @param key
   * @param amount
   * @throws database.StorageException
   * @return
   */
  @throws(classOf[StorageException])
  def increment(key: String, amount: Long): Long = synchronized {
    val test = scala.math.random * (15 - 0)
    if (test < 1)
      throw new StorageException("Simulated store failure " + amount)
    if (db.contains(key)) {
      val oldValue = db.get(key) // SMELL: If you used `contains` why use .get instead of apply?
      if (oldValue.isInstanceOf[Option[Any]]) { // SMELL: All value of type Option[Any] will be instances of Option[Any] :/
        if (oldValue.asInstanceOf[Option[Any]].get.isInstanceOf[Long]) { // SMELL: How can we get a out if we can only add Strings?
          val newValue = amount + oldValue.asInstanceOf[Option[Long]].get // Oh, man Option.get SMELLs so much...
          db += (key -> newValue)
          return newValue // SMELL `return` in scala doesn't do what you think it does, don't use it!
        } else {

          throw new StorageException("value of key " + key + " is not a counter")
        }
      } else {
        throw new StorageException("value of key " + key + " is not a counter")
      }
    } else {
      db += (key -> amount)
      return amount
    }
  }
}
