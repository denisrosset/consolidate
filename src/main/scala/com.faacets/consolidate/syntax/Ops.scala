package com.faacets
package consolidate
package syntax

import scala.language.experimental.macros

import machinist.{DefaultOps => Ops}

final class MergeOps[A](lhs: A)(implicit ev: Merge[A]) {
  def merge(rhs: A): Merged[A] = macro Ops.binop[A, Merged[A]]
}
