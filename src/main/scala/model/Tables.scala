package model

import repository.SlickContext
import slick.jdbc.JdbcProfile
import slick.lifted.{ForeignKeyQuery, ProvenShape}
import slick.lifted.MappedProjection

case class Supplier(id:Int, name:String)
case class Coffee(name:String, supID:Int, price:Double, sales:Int, total:Int)

trait Tables { self:SlickContext=>
  import self.profile.api._

  val suppliers = TableQuery[Suppliers]
  val coffees = TableQuery[Coffees]

  class Suppliers(tag: Tag) extends Table[Supplier](tag, "SUPPLIERS") {
    // This is the primary key column:
    def id = column[Int]("SUP_ID", O.PrimaryKey)

    def name = column[String]("SUP_NAME")

    def * = (id, name) <> (Supplier.tupled, Supplier.unapply)
  }

  // A Coffees table with 5 columns: name, supplier id, price, sales, total
  class Coffees(tag: Tag)
    extends Table[Coffee](tag, "COFFEES") {

    def name = column[String]("COF_NAME", O.PrimaryKey)

    def supID = column[Int]("SUP_ID")

    def price = column[Double]("PRICE")

    def sales = column[Int]("SALES")

    def total = column[Int]("TOTAL")

    def * = (name, supID, price, sales, total) <> (Coffee.tupled, Coffee.unapply)

    // A reified foreign key relation that can be navigated to create a join
    def supplier: ForeignKeyQuery[Suppliers, Supplier] =
      foreignKey("SUP_FK", supID, TableQuery[Suppliers])(_.id)
  }

}