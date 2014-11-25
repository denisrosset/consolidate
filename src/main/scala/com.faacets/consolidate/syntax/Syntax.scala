package com.faacets
package consolidate
package syntax

import scala.language.implicitConversions

trait MergeSyntax {
  implicit def mergeSyntax[A: Merge](a: A) = new MergeOps(a)
}

trait AllSyntax
    extends MergeSyntax
