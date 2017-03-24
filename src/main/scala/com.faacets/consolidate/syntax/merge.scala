package com.faacets.consolidate
package syntax


final class MergeOps[A](val lhs: A) extends AnyVal {

  def merge(rhs: A)(implicit A: Merge[A]): Result[A] = A.merge(lhs, rhs)

}

trait MergeSyntax {

  implicit def mergeSyntaxMerge[A](a: A): MergeOps[A] = new MergeOps[A](a)

}
