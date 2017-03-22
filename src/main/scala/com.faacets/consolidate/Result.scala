package com.faacets.consolidate

import scala.annotation.tailrec

import cats.{Eq, ApplicativeError}
import cats.data.{Validated, ValidatedNel, NonEmptyList => NEL}

import cats.syntax.eq._
import cats.syntax.semigroup._

sealed trait Result[+A] { self =>

  import Result.{Same, Updated, Failed}

  def in(element: String): Result[A]

  def validate[B](f: A => ValidatedNel[String, B]): Result[B] = self match {
    case Same(a) => f(a) match {
      case Validated.Valid(b) => Same(b)
      case Validated.Invalid(errors) => Failed(errors.map(err => (Path.empty, err)))
    }
    case Updated(a, updates) => f(a) match {
      case Validated.Valid(b) => Updated(b, updates)
      case Validated.Invalid(errors) => Failed(errors.map(err => (Path.empty, err)))
    }
    case failed: Failed => failed
  }

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

  protected def appendPath(nel: NEL[(Path, String)], element: String): NEL[(Path, String)] =
    nel.map { case (path, string) => (element :: path, string) }

  case class Same[+A](baseValue: A) extends Result[A] {

    def in(element: String) = this

  }

  case class Updated[+A](newValue: A, updates: NEL[(Path, String)]) extends Result[A] {

    def in(element: String) = Updated(newValue, appendPath(updates, element))

  }

  case class Failed(errors: NEL[(Path, String)]) extends Result[Nothing] {

    def in(element: String) = Failed(appendPath(errors, element))

  }

  type Errors = NEL[(Path, String)]

  implicit val instance: ApplicativeError[Result, NEL[(Path, String)]] = new ApplicativeError[Result, NEL[(Path, String)]] {

    def raiseError[A](e: NEL[(Path, String)]): Result[A] = Failed(e)

    def handleErrorWith[A](fa: Result[A])(f: NEL[(Path, String)] => Result[A]): Result[A] = fa match {
      case r: Same[A] => r
      case r: Updated[A] => r
      case Failed(errors) => f(errors)
    }          

    def pure[A](a: A): Result[A] = Same(a)

    override def map[A, B](fa: Result[A])(f: A => B): Result[B] = fa match {
      case Same(a) => Same(f(a))
      case Updated(a, paths) => Updated(f(a), paths)
      case Failed(errors) => Failed(errors)
    }

    override def ap[A, B](ff: Result[A => B])(fa: Result[A]): Result[B] = (ff, fa) match {
      case (Same(ff1), Same(fa1)) => Same(ff1(fa1))
      case (Same(ff1), Updated(fa1, paths)) => Updated(ff1(fa1), paths)
      case (Updated(ff1, paths), Same(fa1)) => Updated(ff1(fa1), paths)
      case (Updated(ff1, paths1), Updated(fa1, paths2)) => Updated(ff1(fa1), paths1 |+| paths2)
      case (Failed(errors1), Failed(errors2)) => Failed(errors1 |+| errors2)
      case (Failed(errors1), _) => Failed(errors1)
      case (_, Failed(errors2)) => Failed(errors2)
    }

  }

  implicit def consolidateEqForResult[A:Eq]: Eq[Result[A]] = new Eq[Result[A]] {
    import cats.instances.all._
    def eqv(lhs: Result[A], rhs: Result[A]) = (lhs, rhs) match {
      case (Same(a1), Same(a2)) => a1 === a2
      case (Updated(a1, u1), Updated(a2, u2)) => a1 === a2 && u1 === u2
      case (Failed(e1), Failed(e2)) => e1 === e2
      case _ => false
    }

  }

}
