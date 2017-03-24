package com.faacets.consolidate
package instances

import cats.data.NonEmptyList

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



}