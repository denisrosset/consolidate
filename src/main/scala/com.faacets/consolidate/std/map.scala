package com.faacets
package consolidate
package std

import syntax.merge._

final class MapMerge[K, V](implicit V: Merge[V]) extends Merge[Map[K, V]] {

  def merge(current: Map[K, V], other: Map[K, V]): Merged[Map[K, V]] = {
    ((MSame(current): Merged[Map[K, V]]) /: other) {
      case (merged, (otherKey, otherValue)) =>
        val resKeyValue: Merged[(K, V)] = current get otherKey match {
          case None => MNew(otherKey -> otherValue, MLog(Map(List(otherKey.toString) -> s"new value = $otherValue")))
          case Some(currentValue) => (currentValue merge otherValue).map(otherKey -> _).withPath(otherKey.toString)
        }
        for {
          accMap <- merged
          keyValue <- resKeyValue
        } yield accMap + keyValue
    }
  }

}

trait MapInstances {

  implicit def MapMerge[K, V: Merge]: MapMerge[K, V] = new MapMerge[K, V]
  
}
