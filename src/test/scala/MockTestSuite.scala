import model.{Coffee, Coffees, Supplier, Suppliers}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import repository._
import slick.jdbc.H2Profile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

// Test by Mock
class MockTestSuite extends FunSuite with BeforeAndAfter with ScalaFutures {
  implicit override val patienceConfig = PatienceConfig(timeout = Span(5, Seconds))

  // Mock context and Repository
  object MockContext extends RepositoryContext{
    type Action[R] = Option[R]
    type Storage = String
    def success[R](r:R):Action[R] = Some(r)
    def fail(r:Exception):Action[Exception] = None
    def withTransaction[R](db:Storage)(action:Action[R]):Future[R] = Future.successful(action.get)
  }

  class CoffeeRepositoryMock[R](implicit m:Monad[MockContext.Action]) extends CoffeeRepository[MockContext.Action]{

    def findSupplier(id:Int): MockContext.Action[Option[Supplier]] = {
      Some(Some(Supplier(id, "")))
    }

    def saveCoffee(coffee:Coffee):MockContext.Action[Int] = {
      Some(coffee.supID)
    }

    def findCoffee(name:String):MockContext.Action[Option[Coffee]] = {
      Some(Some(Coffee(name, 1, 1.0, 1, 1)))
    }
  }
  // implicit monad instance
  implicit val optionMonadInstance = new Monad[Option] {
    override def map[A, B](fa: Option[A])(f: (A) => B) = fa.map(f)

    override def flatMap[A, B](fa: Option[A])(f: (A) => Option[B]) = fa.flatMap(f)

    override def filter[A](fa: Option[A])(f: (A) => Boolean) = fa.filter(f)
  }

  val ctx = MockContext
  val repo:CoffeeRepository[ctx.Action] = new CoffeeRepositoryMock()

  test("test repository wity mock") {

    val coffee = Coffee("cof", 101, 2.0, 1, 2)

    def existsSupplier(sup:Option[Supplier]) = sup match{
      case Some(s) => true
      case _ => false
    }

    val action = for(
      supplier <- repo.findSupplier(coffee.supID);
      _ <- if(existsSupplier(supplier)) ctx.success("3") else ctx.fail(new RuntimeException("not exists"));
      _ <- repo.saveCoffee(coffee)
    )yield {coffee}

    val res:Future[Coffee] = ctx.withTransaction("")(action)

    val a = Await.result(res,  1.second)

    assert(a.name == "cof")
  }

}