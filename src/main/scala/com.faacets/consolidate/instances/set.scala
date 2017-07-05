package com.faacets.consolidate
package instances

import cats.data.NonEmptyList

import scala.collection.immutable.ListSet

trait SetInstances {

  implicit def consolidateMergeForSet[A] = new Merge[Set[A]] {

    def merge(base: Set[A], other: Set[A]) = {
      val newElements = other -- base
      if (newElements.isEmpty)
        Result.same(base)
      else
        Result.updated(base ++ newElements, NonEmptyList.of(Path.empty -> s"new elements = $newElements"))
    }

  }

  implicit def consolidateMergeForListSet[A] = new Merge[ListSet[A]] {
    def merge(base: ListSet[A], other: ListSet[A]): Result[ListSet[A]] =  {
      val newElements = other -- base
      if (newElements.isEmpty)
        Result.same(base)
      else
        Result.updated(base ++ newElements, NonEmptyList.of(Path.empty -> s"new elements = $newElements"))
    }
  }

}
