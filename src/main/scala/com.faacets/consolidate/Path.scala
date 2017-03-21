package com.faacets.consolidate

final class Path(val revElements: List[String]) extends AnyVal {

  override def toString = revElements.reverse.mkString(".")

}
