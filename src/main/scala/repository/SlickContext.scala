package repository

import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.jdbc.JdbcBackend.DatabaseDef
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future} //TODO DB実装で切り替わる手段は無い?。せめて一箇所にだけ定義できないか?

object SlickContext extends RepositoryContext{
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