package repository

import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.jdbc.JdbcBackend.DatabaseDef
import slick.jdbc.H2Profile.api._ //TODO DB実装で切り替わる手段は無い?。せめて一箇所にだけ定義できないか?


import scala.concurrent.Future


trait RepositoryContext {
  type Action[R]
  def success[R](r:R):Action[R]
  def fail(r:Exception):Action[Exception]
  def withTransaction[R](db:Database)(action:Action[R]):Future[R]
}


object SlickContext extends RepositoryContext{
  type Action[R] = DBIOAction[R, NoStream, Effect.All]
  def success[R](r:R):Action[R] = DBIO.successful(r)
  def fail(r:Exception):Action[Exception] = DBIO.failed(r)
  def withTransaction[R](db:Database)(action:Action[R]):Future[R] = db.run(action.transactionally)
}
