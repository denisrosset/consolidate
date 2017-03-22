package com.faacets.consolidate

import cats.{Eq, Show}
import cats.data.{NonEmptyList => NEL}
import cats.syntax.apply._
import cats.syntax.eq._
import cats.syntax.show._

trait Merge[A] {

  def merge(base: A, other: A): Result[A]

}

object Merge {

  object syntax {

    implicit class MergeOps[A](val lhs: A) extends AnyVal {

      def merge(rhs: A)(implicit ev: Merge[A]): Result[A] = ev.merge(lhs, rhs)

    }

  }

  final def apply[A](implicit ev: Merge[A]): Merge[A] = ev

  private[consolidate] class EqualsMerge[A] extends Merge[A] {
    def merge(base: A, other: A) =
      if (base == other) Result.same(base)
      else Result.failed(NEL.of(Path.empty -> s"$base != $other"))
  }

  private[consolidate] class EqMerge[A:Eq:Show] extends Merge[A] {
    def merge(base: A, other: A) =
      if (base === other) Result.same(base)
      else Result.failed(NEL.of(Path.empty -> s"$base != $other"))
  }

  def fromEquals[A]: Merge[A] = new EqualsMerge[A]

  def fromEq[A:Eq:Show]: Merge[A] = new EqMerge[A]

  implicit def optionMerge[A:Merge]: Merge[Option[A]] = new Merge[Option[A]] {

    def merge(base: Option[A], other: Option[A]) = (base, other) match {
      case (_, None) => Result.same(base)
      case (None, someOther: Some[A]) => Result.updated(someOther, NEL.of((Path.empty -> s"new value = $other")))
      case (Some(base), Some(other)) => Merge[A].merge(base, other).map(Some(_))
    }

  }

  implicit def setMerge[A] = new Merge[Set[A]] {

    def merge(base: Set[A], other: Set[A]) = {
      val newElements = other -- base
      if (newElements.isEmpty)
        Result.same(base)
      else
        Result.updated(base ++ newElements, NEL.of(Path.empty -> s"new elements = $newElements"))
    }

  }

  /*

final class MapMerge[K, V](implicit V: Merge[V]) extends Merge[Map[K, V]] {

  def merge(base: Map[K, V], other: Map[K, V]): Merged[Map[K, V]] = {
    ((MSame(base): Merged[Map[K, V]]) /: other) {
      case (merged, (otherKey, otherValue)) =>
        val resKeyValue: Merged[(K, V)] = base get otherKey match {
          case None => MNew(otherKey -> otherValue, MLog(Map(List(otherKey.toString) -> s"new value = $otherValue")))
          case Some(baseValue) => (baseValue merge otherValue).map(otherKey -> _).withPath(otherKey.toString)
        }
        for {
          accMap <- merged
          keyValue <- resKeyValue
        } yield accMap + keyValue
    }
  }

}

   */
}
