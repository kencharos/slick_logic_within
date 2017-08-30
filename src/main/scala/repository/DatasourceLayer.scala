package repository

import model.Tables
import slick.jdbc.JdbcProfile

class DatasourceLayer(val prof:JdbcProfile)
  extends SlickContext
    with Tables
    with SlickRepositories{

   override val profile = prof

}
