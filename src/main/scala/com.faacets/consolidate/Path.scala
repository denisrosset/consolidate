package com.faacets.consolidate

import cats.Eq
import cats.instances.all._
import cats.syntax.eq._

final case class Path(val elements: List[String]) extends AnyVal {

  def :::(leftElements: List[String]): Path = Path(leftElements ::: elements)

  def ::(leftElement: String): Path = Path(leftElement :: elements)

  override def toString = elements.mkString(".")

}

object Path {

  val empty = new Path(Nil)

  implicit val eqPath: Eq[Path] = new Eq[Path] {

    def eqv(a: Path, b: Path) = a.elements === b.elements

  }

}
