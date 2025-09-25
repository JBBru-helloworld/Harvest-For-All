package harvestforall.utilities

import scala.collection.mutable.ListBuffer

/** Observable trait for implementing the Observer pattern
  *
  * Allows objects to notify observers of state changes - classic GoF design
  * pattern! Way cleaner than manually managing listener lists everywhere
  */
trait Observable[T]:
  private val observers: ListBuffer[T => Unit] = ListBuffer.empty

  /** Add an observer to be notified of changes
    */
  def addObserver(observer: T => Unit): Unit =
    observers += observer

  /** Remove an observer
    */
  def removeObserver(observer: T => Unit): Unit =
    observers -= observer

  /** Notify all observers of a change - this is where the magic happens
    */
  def notifyObservers(data: T): Unit =
    observers.foreach(_(data))

  /** Get number of observers (useful for debugging)
    */
  def observerCount: Int = observers.size

  /** Clear all observers - helps prevent memory leaks
    */
  def clearObservers(): Unit =
    observers.clear()
