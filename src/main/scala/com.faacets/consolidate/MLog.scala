package com.faacets
package consolidate

case class MLog(entries: Map[List[String], String]) extends AnyVal {

  def withPath(pathElement: String) = MLog(entries.map { case (k, v) => ((pathElement :: k) -> v) })

  def ++(rhs: MLog): MLog = MLog(entries ++ rhs.entries)

  override def toString = entries.map { case (k, v) => k.reverse.mkString(".") + ": " + v }.mkString("MLog(", ", ", ")")

}

object MLog {

  def empty: MLog = MLog(Map.empty[List[String], String])
  
}
