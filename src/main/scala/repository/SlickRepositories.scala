package repository

import model.{Coffee, Supplier, Tables}


trait SlickRepositories { self:SlickContext =>

  import self.profile.api._ //for slick syntax
  import self._ // for implicit monad instance

  /** Repository Implementation by Slick */
  class CoffeeRepositoryImpl[R](tables:Tables)(implicit monad: Monad[self.Action])
    extends CoffeeRepository[self.Action]{

    def findSupplier(id:Int):self.Action[Option[Supplier]] = {
      tables.suppliers.filter(_.id === id).result.headOption
    }

    def saveCoffee(coffee:Coffee) = {
      tables.coffees += coffee
    }

    def findCoffee(name:String) = {
      tables.coffees.filter(_.name === name).result.headOption
    }
  }

}

