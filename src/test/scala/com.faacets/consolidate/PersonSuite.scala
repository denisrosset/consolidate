package com.faacets
package consolidate

import org.scalatest.{FunSuite, Inside, Matchers}
import cats.data.{Validated, ValidatedNel, NonEmptyList => NEL}
import Result.{Failed, Same, Updated}
import cats.syntax.all._

import com.faacets.consolidate.implicits._

class PersonSuite extends FunSuite with Matchers with Inside {

  import PersonSuite._

  test("Merge syntax and results") {
    val a = Person("Jack", None, None)
    val b = Person("Jack", Some(40), None)
    (a merge a) shouldBe Same(a)
    inside(a merge b) { case Updated(c, _) => c shouldBe b }
    (b merge a) shouldBe Same(b)
  }

  test("Map merge") {
    val a = Person("Jack", None, None)
    val b = Person("Jack", Some(40), None)
    inside(Map("a" -> a) merge Map("b" -> b)) {
      case Updated(newMap, _) => newMap shouldBe Map("a" -> a, "b" -> b)
    }
    inside(Map("a" -> a) merge Map("a" -> b)) {
      case Updated(newMap, _) => newMap shouldBe Map("a" -> b)
    }
  }

}

object PersonSuite {

  case class Person(name: String, age: Option[Int] = None, retired: Option[Boolean] = None)

  object Person {

    def validated(name: String, age: Option[Int], retired: Option[Boolean]): ValidatedNel[String, Person] = {

      import Validated.{invalidNel, valid}

      def validName(name: String) = if (name.isEmpty) invalidNel("Name cannot be empty") else valid(name)
      def validAge(age: Option[Int]) = age match {
        case Some(a) if a < 0 => invalidNel("Age cannot be negative")
        case _ => valid(age)
      }

      (validName(name) |@| validAge(age) |@| valid(retired)).map(Person.apply)

    }

    implicit def StringMerge = Merge.fromEquals[String]

    implicit def IntMerge = Merge.fromEquals[Int]

    implicit def BooleanMerge = Merge.fromEquals[Boolean]

    implicit val PersonMerge: Merge[Person] = Auto.derive[Person].validated((Person.validated _).tupled)

  }

}