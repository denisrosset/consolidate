package com.faacets
package consolidate
package std

import syntax.merge._

final class OptionMerge[A: Merge] extends Merge[Option[A]] {

  def merge(current: Option[A], other: Option[A]) = (current, other) match {
    case (_, None) => MSame(current)
    case (None, Some(o)) => MNew(other, MLog(Map(Nil -> s"new value = $o")))
    case (Some(c), Some(o)) => c.merge(o).map(Some(_))
  }

}

trait OptionInstances {

  implicit def OptionMerge[A: Merge]: OptionMerge[A] = new OptionMerge[A]

}
