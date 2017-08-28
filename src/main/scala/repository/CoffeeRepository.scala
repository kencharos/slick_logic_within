package repository

import model.{Coffee, Supplier}
import slick.dbio.{DBIOAction, Effect, NoStream}


trait CoffeeRepository {

  type Action[R] = DBIOAction[R, NoStream, Effect.All]

  def findSupplier(id:Int):Action[Option[Supplier]]

  def saveCoffee(coffee:Coffee):Action[Int]

  def findCoffee(name:String):Action[Option[Coffee]]

}
