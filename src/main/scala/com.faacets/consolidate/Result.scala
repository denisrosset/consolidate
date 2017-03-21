package com.faacets
package consolidate

import cats.Apply
import cats.data.{NonEmptyList => NEL}

import cats.syntax.semigroup._

sealed trait Result[+A] { self =>

  import Result.{Same, Updated, Failed}

  def isSame: Boolean = self match {
    case _: Same[_] => true
    case _: Updated[_] => false
    case _: Failed => false
  }

  def isUpdated: Boolean = self match {
    case _: Updated[_] => true
    case _: Same[_] => false
    case _: Failed => false
  }

  def hasFailed: Boolean = self match {
    case _: Failed => true
    case _: Updated[_] => false
    case _: Same[_] => false
  }

  def value: Option[A] = self match {
    case Same(a) => Some(a)
    case Updated(a, _) => Some(a)
    case _: Failed => None
  }

  def fold[X](
    same: A => X,
    updated: (A, NEL[(Path, String)]) => X,
    failed: NEL[(Path, String)] => X
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

  def check(path: Path, f: A => List[String]): Result[A] = self match {
    case Same(value) => NEL.fromList(f(value)) match {
      case None => self
      case Some(errors) => throw new Exception("Should not happen: base element is inconsistent, with errors: " + errors.toString)
    }
    case Updated(newValue, updates) => NEL.fromList(f(newValue)) match {
      case None => self
      case Some(errors) => Failed(errors.map(error => (path, error)))
    }
    case failed: Failed => failed
  }

}

object Result {

  def same[A](baseValue: A): Result[A] = Same(baseValue)

  def updated[A](newValue: A, updates: NEL[(Path, String)]): Result[A] = Updated(newValue, updates)

  def failed[A](errors: NEL[(Path, String)]): Result[A] = Failed(errors)

  private[consolidate] case class Same[+A](baseValue: A) extends Result[A]

  private[consolidate] case class Updated[+A](newValue: A, updatedPaths: NEL[(Path, String)]) extends Result[A]

  private[consolidate] case class Failed(errors: NEL[(Path, String)]) extends Result[Nothing]

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
