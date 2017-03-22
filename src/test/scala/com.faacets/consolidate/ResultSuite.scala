package com.faacets.consolidate

import org.scalatest.{FunSuite, Matchers}
import org.scalacheck.{Arbitrary, Cogen}

import org.typelevel.discipline.scalatest.Discipline

import cats.instances.all._
import cats.laws.discipline.arbitrary._
import cats.laws.discipline.MonadErrorTests


class ResultSuite extends FunSuite with Matchers with Discipline {

  implicit val arbError: Arbitrary[(Path, String)] = Arbitrary {
    implicitly[Arbitrary[String]].arbitrary.map(err => (Path.empty, err))
  }

  implicit def arbResult[A:Arbitrary]: Arbitrary[Result[A]] =
    Arbitrary { implicitly[Arbitrary[A]].arbitrary.map(Result.same(_)) }

  implicit def cogenError: Cogen[Path] = Cogen(_.hashCode)

  checkAll("Result", MonadErrorTests[Result, Result.Errors].monadError[Int, String, Long])

}
