package com.faacets.consolidate

import org.scalatest.{FunSuite, Matchers}
import org.scalacheck.{Arbitrary, Cogen, Gen}
import Arbitrary.arbitrary

import org.typelevel.discipline.scalatest.Discipline

import cats.instances.all._
import cats.laws.discipline.arbitrary._
import cats.laws.discipline.MonadErrorTests
import cats.data.{NonEmptyList => NEL}

class ResultSuite extends FunSuite with Matchers with Discipline {

  implicit val arbErrorOrUpdate: Arbitrary[(Path, String)] = Arbitrary {
    Gen.oneOf("err", "fail", "lol").map(str => (Path.empty, str))
  }

  implicit def arbResult[A:Arbitrary]: Arbitrary[Result[A]] =
    Arbitrary {
      Gen.oneOf(
        for (a <- arbitrary[A]) yield Result.same(a),
        for (a <- arbitrary[A]; up <- arbErrorOrUpdate.arbitrary) yield Result.updated(a, NEL.of(up)),
        for (err <- arbErrorOrUpdate.arbitrary) yield Result.failed(NEL.of(err))
      )
    }

  implicit def cogenError: Cogen[Path] = Cogen(_.hashCode)

  checkAll("Result", MonadErrorTests[Result, Result.Errors].monadError[Int, String, Long])

}
