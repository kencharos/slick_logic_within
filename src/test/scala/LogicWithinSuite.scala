import model.{Coffee, Coffees, Supplier, Suppliers}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import repository.CoffeeRepositoryImpl
import slick.jdbc.H2Profile.api._
import scala.concurrent.ExecutionContext.Implicits.global

import slick.jdbc.meta._

class LogicWithinSuite extends FunSuite with BeforeAndAfter with ScalaFutures {
  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  val suppliers = TableQuery[Suppliers]
  val coffees = TableQuery[Coffees]

  var db: Database = _

  def createSchema() =
    db.run((suppliers.schema ++ coffees.schema).create).futureValue

  def insertSupplier(): Int =
    db.run(suppliers += Supplier(101, "Acme, Inc.")).futureValue

  before { db = Database.forConfig("test") }

  test("Insert coffee exists supplier") {
    createSchema()
    insertSupplier()

    val repo = new CoffeeRepositoryImpl()
    val coffee = Coffee("cof", 101, 2.0, 1, 2)

    def existsSupplier(sup:Option[Supplier]) = DBIO.seq(sup match{
      case Some(s) => DBIO.successful(true)
      case _ => DBIO.failed(new RuntimeException("not exists"))
    })

    def existsSupplier2(sup:Option[Supplier]) = sup match{
      case Some(s) => true
      case _ => false
    }

    val action = for(
      supplier <- repo.findSupplier(coffee.supID);
      _ <- DBIO.seq(if(existsSupplier2(supplier)) DBIO.successful("3") else DBIO.failed(new RuntimeException("not exists")));
      //_ <- existsSupplier(supplier);
      _ <- repo.saveCoffee(coffee)
    )yield {coffee}
    //execute logic
    db.run(action.transactionally).futureValue

    val results = db.run(coffees.result).futureValue
    assert(results.size == 1)
    assert(results.head.name == "cof")
  }


  test("Occuer Excetion and rollback when coffee not exists supplier") {
    createSchema()
    insertSupplier()

    val repo = new CoffeeRepositoryImpl()
    val coffee = Coffee("cof", 101, 2.0, 1, 2)
    val coffee2 = Coffee("cof2", 102, 2.0, 1, 2)

    def existsSupplier(sup:Option[Supplier]) = DBIO.seq(sup match{
      case Some(s) => DBIO.successful(true)
      case _ => DBIO.failed(new RuntimeException("not exists"))
    })


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
      db.run(action).futureValue
    } catch {
      case e:RuntimeException => {e.printStackTrace();assert(e.getMessage contains "not exists")}
    }

    val results = db.run(coffees.result).futureValue
    assert(results.size == 0) // must rollback
  }

  after { db.close }
}