package com.faacets
package consolidate
package syntax

final class MergeOps[A](val lhs: A) extends AnyVal {

  def merge(rhs: A)(implicit ev: Merge[A]): Merged[A] = ev.merge(lhs, rhs)

}
