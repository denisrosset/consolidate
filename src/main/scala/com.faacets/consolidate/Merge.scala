package com.faacets
package consolidate

import cats.{Eq, Show}
import cats.data.{NonEmptyList => NEL}
import cats.syntax.apply._
import cats.syntax.eq._
import cats.syntax.show._

trait Merge[A] {

  def merge(path: Path, current: A, other: A): Result[A]

}

object Merge {

  final def apply[A](implicit ev: Merge[A]): Merge[A] = ev

  private[consolidate] class EqualsMerge[A] extends Merge[A] {
    def merge(path: Path, current: A, other: A) =
      if (current == other) Result.same(current)
      else Result.failed(NEL.of(path -> s"$current != $other"))
  }

  private[consolidate] class EqMerge[A:Eq:Show] extends Merge[A] {
    def merge(path: Path, current: A, other: A) =
      if (current === other) Result.same(current)
      else Result.failed(NEL.of(path -> s"$current != $other"))
  }

  def fromEquals[A]: Merge[A] = new EqualsMerge[A]

  def fromEq[A:Eq:Show]: Merge[A] = new EqMerge[A]

  implicit def OptionMerge[A:Merge]: Merge[Option[A]] = new Merge[Option[A]] {

    def merge(path: Path, current: Option[A], other: Option[A]) = (current, other) match {
      case (_, None) => Result.same(current)
      case (None, someOther: Some[A]) => Result.updated(someOther, NEL.of((path -> "new value = $other")))
      case (Some(current), Some(other)) => Merge[A].merge(path, current, other).map(Some(_))
    }

  }

}
