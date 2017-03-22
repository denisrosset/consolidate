package com.faacets
package consolidate

import org.scalatest.{FunSuite, Inside, Matchers}

import cats.data.{NonEmptyList => NEL, Validated, ValidatedNel}

import Merge.syntax._

import Result.{Same, Updated, Failed}

import cats.syntax.all._

case class Person(name: String, age: Option[Int], retired: Option[Boolean])

object Person {

  def validated(name: String, age: Option[Int], retired: Option[Boolean]): ValidatedNel[String, Person] = {

    import Validated.{invalidNel, valid}

    def validName(name: String): ValidatedNel[String, String] = if (name.isEmpty) invalidNel("Name cannot be empty") else valid(name)
    def validAge(age: Option[Int]): ValidatedNel[String, Option[Int]] = age.fold(valid(age): ValidatedNel[String, Option[Int]])(a => if (a < 0) invalidNel("Age cannot be negative") else valid(age))

    (validName(name) |@| validAge(age) |@| valid(retired)).map(Person.apply)

  }

  implicit def StringMerge = Merge.fromEquals[String]

  implicit def IntMerge = Merge.fromEquals[Int]

  implicit def BooleanMerge = Merge.fromEquals[Boolean]

  def error1[A, B](a: A): ValidatedNel[String, B] = Validated.invalidNel("Error")

  implicit val PersonMerge: Merge[Person] = Auto.derive[Person].validated((Person.validated _).tupled)

//  lazy val PersonMerge: Merge[Person] = Auto[Person]

/*  implicit object PersonMerge extends Merge[Person] {

    def merge(current: Person, other: Person) =
      (
        current.name.merge(other.name).in("name") |@|
          current.age.merge(other.age).in("age") |@|
          current.retired.merge(other.retired).in("retired")
      ).map(Person(_,_,_))

  }*/

}

class PersonSuite extends FunSuite with Matchers with Inside {

  test("Merge syntax and results") {
    val a = Person("Jack", None, None)
    val b = Person("Jack", Some(40), None)
    (a merge a) shouldBe Same(a)
    inside(a merge b) { case Updated(c, _) => c shouldBe b }
    (b merge a) shouldBe Same(b)
  }

}
