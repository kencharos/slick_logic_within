import model.{Coffee, Coffees, Supplier, Suppliers}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import repository.{CoffeeRepository, CoffeeRepositoryImpl, RepositoryContext, SlickContext}
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global


class LogicWithinSuite extends FunSuite with BeforeAndAfter with ScalaFutures {
  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  val suppliers = TableQuery[Suppliers]
  val coffees = TableQuery[Coffees]

  // fixed Implementation to Slick
  val ctx = SlickContext
  val repo:CoffeeRepository[ctx.Action] = new CoffeeRepositoryImpl()

  var db: Database = _

  def createSchema() =
    db.run((suppliers.schema ++ coffees.schema).create).futureValue

  def insertSupplier(): Int =
    db.run(suppliers += Supplier(101, "Acme, Inc.")).futureValue

  before { db = Database.forConfig("test") }

  test("Insert coffee exists supplier") {
    createSchema()
    insertSupplier()

    val coffee = Coffee("cof", 101, 2.0, 1, 2)

    def existsSupplier2(sup:Option[Supplier]) = sup match{
      case Some(s) => true
      case _ => false
    }

    val action = for(
      supplier <- repo.findSupplier(coffee.supID);
      _ <- if(existsSupplier2(supplier)) ctx.success("3") else ctx.fail(new RuntimeException("not exists"));
      _ <- repo.saveCoffee(coffee)
    )yield {coffee}
    //execute logic

    val res = ctx.withTransaction(db)(action)

    res.futureValue

    val results = db.run(coffees.result).futureValue
    assert(results.size == 1)
    assert(results.head.name == "cof")
  }


  test("Occuer Excetion and rollback when coffee not exists supplier") {
    createSchema()
    insertSupplier()

    val coffee = Coffee("cof", 101, 2.0, 1, 2)
    val coffee2 = Coffee("cof2", 102, 2.0, 1, 2)

    def existsSupplier(sup:Option[Supplier]) = sup match{
      case Some(s) => ctx.success(true)
      case _ => ctx.fail(new RuntimeException("not exists"))
    }

    val action = for(
      supplier <- repo.findSupplier(coffee.supID);
      _ <- existsSupplier(supplier);
      _ <- repo.saveCoffee(coffee);
      supplier2 <- repo.findSupplier(coffee2.supID);
      _ <- existsSupplier(supplier2);
      _ <- repo.saveCoffee(coffee2)
    )yield{(coffee, coffee2)}
    //execute logic
    try {

      ctx.withTransaction(db)(action).futureValue
    } catch {
      case e:RuntimeException => {e.printStackTrace();assert(e.getMessage contains "not exists")}
    }

    val results = db.run(coffees.result).futureValue
    assert(results.size == 0) // must rollback
  }

  after { db.close }
}