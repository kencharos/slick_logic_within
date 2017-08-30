package repository

import model.{Tables}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait SlickContext extends RepositoryContext with Tables{

  val profile:JdbcProfile

  import profile.api._

  type Action[R] = DBIOAction[R, NoStream, Effect.All]
  type Storage = Database
  def success[R](r:R):Action[R] = DBIO.successful(r)
  def fail(r:Exception):Action[Exception] = DBIO.failed(r)
  def withTransaction[R](db:Storage)(action:Action[R]):Future[R] = db.run(action.transactionally)

  implicit def DBIOMonadInstance(implicit ec:ExecutionContext) = new Monad[Action] {
    override def map[A, B](fa: DBIOAction[A, NoStream, Effect.All])(f: (A) => B) = fa.map(f)

    override def flatMap[A, B](fa: DBIOAction[A, NoStream, Effect.All])(f: (A) => DBIOAction[B, NoStream, Effect.All]) = fa.flatMap(f)

    override def filter[A](fa: DBIOAction[A, NoStream, Effect.All])(f: (A) => Boolean) = fa.filter(f)
  }
}