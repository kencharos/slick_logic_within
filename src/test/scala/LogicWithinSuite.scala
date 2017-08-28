import model.{Coffee, Coffees, Supplier, Suppliers}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import slick.jdbc.H2Profile.api._
import slick.jdbc.meta._

class TablesSuite extends FunSuite with BeforeAndAfter with ScalaFutures {
  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  val suppliers = TableQuery[Suppliers]
  val coffees = TableQuery[Coffees]

  var db: Database = _

  def createSchema() =
    db.run((suppliers.schema ++ coffees.schema).create).futureValue

  def insertSupplier(): Int =
    db.run(suppliers += Supplier(101, "Acme, Inc.")).futureValue

  def insertCoffee(supId:Int): Int =
    db.run(coffees += Coffee("cof", supId, 1.0, 4, 4)).futureValue

  before { db = Database.forConfig("test") }

  test("Inserting a Supplier works") {
    createSchema()

    val insertCount = insertSupplier()
    assert(insertCount == 1)
  }

  test("Query Suppliers works") {
    createSchema()
    insertSupplier()
    val results = db.run(suppliers.result).futureValue
    assert(results.size == 1)
    assert(results.head.id == 101)

  }

  test("Query Coffee works") {
    createSchema()
    insertSupplier()
    insertCoffee(101)
    val results = db.run(coffees.result).futureValue
    assert(results.size == 1)
    assert(results.head.name == "cof")

  }
  after { db.close }
}