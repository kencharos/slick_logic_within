package repository

import model.{Coffee, Coffees, Supplier, Suppliers}
import slick.dbio.{DBIOAction, NoStream}
import slick.lifted.TableQuery
import slick.jdbc.H2Profile.api._

class CoffeeRepositoryImpl extends CoffeeRepository{

  val suppliers = TableQuery[Suppliers]
  val coffees = TableQuery[Coffees]

  def findSupplier(id:Int):Action[Option[Supplier]] = {
    suppliers.filter(_.id === id).result.headOption
  }

  def saveCoffee(coffee:Coffee):Action[Int] = {
    coffees += coffee
  }

  def findCoffee(name:String):Action[Option[Coffee]] = {
    coffees.filter(_.name === name).result.headOption
  }
}
