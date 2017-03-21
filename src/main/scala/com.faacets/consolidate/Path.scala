package com.faacets.consolidate

final case class Path(val revElements: List[String]) extends AnyVal {

  override def toString = revElements.reverse.mkString(".")

}

object Path {

  val root = new Path(Nil)

}
