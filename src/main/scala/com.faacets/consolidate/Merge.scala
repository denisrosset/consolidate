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

  final class MapMerge[V](implicit V: Merge[V]) extends Merge[Map[String, V]] {

    import cats.syntax.all._

    import Result.{same, updated, failed}

    def merge(base: Map[String, V], other: Map[String, V]): Result[Map[String, V]] = {
      ((same(base): Result[Map[String, V]]) /: other) {
        case (merged, (otherKey, otherValue)) =>
          val res: Result[(String, V)] = base get otherKey match {
            case None => updated(otherKey -> otherValue, NEL.of(Path.empty -> s"new value for key $otherKey = $otherValue"))
            case Some(baseValue) => V.merge(baseValue, otherValue).map(otherKey -> _)
          }
          (merged |@| res.in(otherKey)).map { case (accMap, kv) => accMap + kv }
      }
    }

  }

  implicit def mapMerge[V:Merge]: Merge[Map[String, V]] = new MapMerge

}
