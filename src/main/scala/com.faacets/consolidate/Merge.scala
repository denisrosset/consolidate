package com.faacets.consolidate

import cats.{Eq, Show}
import cats.data.{NonEmptyList => Nel}
import cats.syntax.apply._
import cats.syntax.eq._

trait Merge[A] {

  def merge(base: A, other: A): Result[A]

}

object Merge {

  final def apply[A](implicit ev: Merge[A]): Merge[A] = ev

  def fromEquals[A]: Merge[A] = new Merge[A] {

    def merge(base: A, other: A) =
      if (base == other) Result.same(base)
      else Result.failed(Nel.of(Path.empty -> s"$base != $other"))

  }

  def fromEq[A:Eq]: Merge[A] = new Merge[A] {

    def merge(base: A, other: A) =
      if (base === other) Result.same(base)
      else Result.failed(Nel.of(Path.empty -> s"$base != $other"))

  }

}
