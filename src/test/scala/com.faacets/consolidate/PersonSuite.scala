package com.faacets
package consolidate

import org.scalatest.{FunSuite, Inside, Matchers}

import Merge.syntax._

import Result.{Same, Updated, Failed}

import cats.syntax.all._

case class Person(name: String, age: Option[Int], retired: Option[Boolean])

object Person {


  implicit def StringMerge = Merge.fromEquals[String]

  implicit def IntMerge = Merge.fromEquals[Int]

  implicit def BooleanMerge = Merge.fromEquals[Boolean]

  implicit val PersonMerge: Merge[Person] = Auto.derive[Person].noValidation

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
