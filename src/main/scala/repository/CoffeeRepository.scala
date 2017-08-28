package repository

import model.{Coffee, Supplier}
import slick.dbio.{DBIOAction, Effect, NoStream}
import scala.language.higherKinds

trait CoffeeRepository[F[_]] {

  def findSupplier(id:Int):F[Option[Supplier]]

  def saveCoffee(coffee:Coffee):F[Int]

  def findCoffee(name:String):F[Option[Coffee]]

}
