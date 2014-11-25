package com.faacets
package consolidate
package algebra

import spire.algebra.Eq

trait Merge[A] {
  def merge(current: A, other: A): Merged[A]
}

object Merge {
  class EqualsMerge[A] extends Merge[A] {
    def merge(current: A, other: A) =
      if (current == other) MSame(current) else MFail(current, MLog(Map(Nil -> s"$current != $other")))
  }
  def fromEquals[A] = new EqualsMerge[A]
}
