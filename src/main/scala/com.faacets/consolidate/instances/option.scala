package com.faacets.consolidate
package instances

import cats.data.{NonEmptyList => Nel}

trait OptionInstances {

  implicit def optionMerge[A:Merge]: Merge[Option[A]] = new Merge[Option[A]] {

    def merge(base: Option[A], other: Option[A]) = (base, other) match {
      case (_, None) => Result.same(base)
      case (None, someOther: Some[A]) => Result.updated(someOther, Nel.of((Path.empty -> s"new value = $other")))
      case (Some(base), Some(other)) => Merge[A].merge(base, other).map(Some(_))
    }

  }

}

