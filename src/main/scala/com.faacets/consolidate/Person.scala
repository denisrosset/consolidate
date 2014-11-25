package com.faacets
package consolidate

import syntax.all._
import std.any._

case class Person(name: String, age: Option[Int], retired: Option[Boolean])

object Person {
  implicit def StringMerge = Merge.fromEquals[String]
  implicit def IntMerge = Merge.fromEquals[Int]
  implicit def BooleanMerge = Merge.fromEquals[Boolean]
  implicit object PersonMerge extends Merge[Person] {
    def merge(current: Person, other: Person) =
      for {
        name <- current.name.merge(other.name).withPath("name")
        age <- current.age.merge(other.age).withPath("age")
        retired <- current.retired.merge(other.retired).withPath("retired")
      } yield Person(name, age, retired)
  }
}
