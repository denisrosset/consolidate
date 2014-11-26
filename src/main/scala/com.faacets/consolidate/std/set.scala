package com.faacets
package consolidate
package std

final class SetMerge[A] extends Merge[Set[A]] {
  def merge(current: Set[A], other: Set[A]): Merged[Set[A]] = {
    val newElements = other -- current
    if (newElements.isEmpty) MSame(current) else MNew(current ++ newElements, MLog(newElements.map(el => (Nil -> s"new element = $el")).toMap))
  }
}

trait SetInstances {
  implicit def SetMerge[A]: SetMerge[A] = new SetMerge[A]
}
