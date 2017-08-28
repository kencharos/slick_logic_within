package repository

import scala.concurrent.Future

/** Context Abstraction */
trait RepositoryContext {
  /** type for Storage IO Action */
  type Action[R]
  /** type for infra storage Implementation */
  type Storage
  /** Unit value for success  */
  def success[R](r:R):Action[R]
  /** Unit value for fail  */
  def fail(r:Exception):Action[Exception]
  /** execute Storage IO actions within transaction and return result which is wrapped by Future. */
  def withTransaction[R](storage:Storage)(action:Action[R]):Future[R]
}

