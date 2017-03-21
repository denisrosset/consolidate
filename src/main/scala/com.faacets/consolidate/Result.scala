package com.faacets
package consolidate

import cats.Apply
import cats.data.NonEmptyList

import cats.syntax.semigroup._

sealed trait Result[+A] { self =>

  import Result.{Same, Updated, Failed}

  def fold[X](
    same: A => X,
    updated: (A, NonEmptyList[(Path, String)]) => X,
    failed: NonEmptyList[(Path, String)] => X
  ): X = self match {
    case Same(value) => same(value)
    case Updated(newValue, updates) => updated(newValue, updates)
    case Failed(errors) => failed(errors)
  }

  def map[B](f: A => B): Result[B] = self match {
    case Same(value) => Same(f(value))
    case Updated(newValue, updates) => Updated(f(newValue), updates)
    case failed: Failed => failed
  }

}

object Result {

  def same[A](value: A): Result[A] = Same(value)

  def updated[A](newValue: A, updates: NonEmptyList[(Path, String)]): Result[A] = Updated(newValue, updates)

  def failed[A](errors: NonEmptyList[(Path, String)]): Result[A] = Failed(errors)

  private[consolidate] case class Same[+A](value: A) extends Result[A]

  private[consolidate] case class Updated[+A](newValue: A, updatedPaths: NonEmptyList[(Path, String)]) extends Result[A]

  private[consolidate] case class Failed(errors: NonEmptyList[(Path, String)]) extends Result[Nothing]

  implicit val consolidateApplyForResult: Apply[Result] = new Apply[Result] {

    def map[A, B](fa: Result[A])(f: A => B): Result[B] = fa match {
      case Same(a) => Same(f(a))
      case Updated(a, paths) => Updated(f(a), paths)
      case Failed(errors) => Failed(errors)
    }

    def ap[A, B](ff: Result[A => B])(fa: Result[A]): Result[B] = (ff, fa) match {
      case (Same(ff1), Same(fa1)) => Same(ff1(fa1))
      case (Same(ff1), Updated(fa1, paths)) => Updated(ff1(fa1), paths)
      case (Updated(ff1, paths), Same(fa1)) => Updated(ff1(fa1), paths)
      case (Updated(ff1, paths1), Updated(fa1, paths2)) => Updated(ff1(fa1), paths1 |+| paths2)
      case (Failed(errors1), Failed(errors2)) => Failed(errors1 |+| errors2)
      case (Failed(errors1), _) => Failed(errors1)
      case (_, Failed(errors2)) => Failed(errors2)
    }

  }

}
