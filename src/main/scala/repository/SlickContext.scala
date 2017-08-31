package repository

import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait SlickContext{

  val profile:JdbcProfile

  import profile.api._

  type Action[R] = DBIO[R]
  type Storage = Database
  def success[R](r:R):Action[R] = DBIO.successful(r)
  def fail(r:Exception):Action[Exception] = DBIO.failed(r)
  def withTransaction[R](db:Storage)(action:Action[R]):Future[R] = db.run(action.transactionally)

  implicit def DBIOMonadInstance(implicit ec:ExecutionContext) = new Monad[Action] {
    override def map[A, B](fa:Action[A])(f: (A) => B) = fa.map(f)

    override def flatMap[A, B](fa:Action[A])(f: (A) => Action[B]) = fa.flatMap(f)

    override def filter[A](fa:Action[A])(f: (A) => Boolean) = fa.filter(f)
  }
}