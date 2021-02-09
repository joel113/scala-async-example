package com.joel.scala.async

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.Random

/**
 * Some examples for async programming using futures and promises.
 *
 * Please refer to
 *
 * https://docs.scala-lang.org/overviews/core/futures.html
 *
 * for more information.
 */
object Main extends App {

  Thread.sleep(20000)

  // the execution context decides were a computation is executed
  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global

  def compute(id: Int): Int = {
    Thread.sleep(Random.nextInt(10000))
    id + 1
  }

  // traditional fashion
  val id = 1
  compute(id)

  /**
   * Returns a future for the validation response.
   *
   * @param id to become validated
   * @return future for the validation response
   */
  def computeFuture(id: Int): Future[Int] = {
    // Future.apply creates and schedules an asynchronous computation
    Future {
      Thread.sleep(Random.nextInt(10000))
      id + 1
    }
  }

  // creates a new future by applying the result of the future to the map function
  computeFuture(id).map {
    "The computation result is " + _ + "."
  }

  // creates a new future by applying the result of the first future to the flat map and the second map while getting
  // rid of the nested future structure
  computeFuture(id).flatMap(x => computeFuture(x).map {
    y => "The computation result is " + x + y + "."
  })

  // the flatmap and map combination is the same as the following for-comprehension
  for {
    x: Int <- computeFuture(id)
    y: Int <- computeFuture(x)
  } yield "The computation result is " + x + y + "."


  // the flatmap operation is really nice if there are future operations after the initial future
  val condition = Future(true)
  def action1(): Future[Unit] = Future()
  def action2(): Future[Unit] = Future()
  condition.flatMap(
    if(_)
      action1()
    else
      action2()
  )

  val condition1 = Future(true)
  val condition2 = Future(true)
  val condition3 = Future(true)

  implicit class FutureOfBooleanExtensions(val future: Future[Boolean]) extends AnyVal {
    def &&(other: => Future[Boolean]): Future[Boolean] =
      future.flatMap(value => if (!value) Future.successful(false) else other)

    def ||(other: => Future[Boolean]): Future[Boolean] =
      future.flatMap(value => if (value) Future.successful(true) else other)
  }

  def condition11: Future[Boolean] = Future(true)
  def condition22: Future[Boolean] = Future(true)
  def condition33: Future[Boolean] = Future(true)
  val conditionEvaluation = condition11 && (condition22 || condition33)
  conditionEvaluation.flatMap(
    if (_)
      action1()
    else
      action2()
  )

}
