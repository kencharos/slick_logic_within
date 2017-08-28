package repository

import scala.language.higherKinds
/**
  * this type class is minimum monad to use for-comprehension, so that this does not define unit method.
  */
trait Monad[F[_]] {
  def map[A,B](fa:F[A])(f:A=>B):F[B]
  def flatMap[A,B](fa:F[A])(f:A=>F[B]):F[B]
  def filter[A](fa:F[A])(f:A=>Boolean):F[A]
}