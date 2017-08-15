package com.faacets.consolidate
package instances

import scala.collection.immutable.{ListMap, Map}

import cats.data.{NonEmptyList => Nel}
import cats.syntax.all._

import Result.{same, updated, failed}

trait MapInstances {

  implicit def mapMergeString[V:Merge]: Merge[Map[String, V]] = new Merge[Map[String, V]] {

    def merge(base: Map[String, V], other: Map[String, V]): Result[Map[String, V]] = {
      ((same(base): Result[Map[String, V]]) /: other) {
        case (merged, (otherKey, otherValue)) =>
          val res: Result[(String, V)] = base get otherKey match {
            case None => updated(otherKey -> otherValue, Nel.of(Path.empty -> s"new value for key $otherKey = $otherValue"))
            case Some(baseValue) => Merge[V].merge(baseValue, otherValue).map(otherKey -> _)
          }
          (merged |@| res.in(otherKey)).map { case (accMap, kv) => accMap + kv }
      }
    }

  }

  implicit def listMapMergeString[V:Merge]: Merge[ListMap[String, V]] = new Merge[ListMap[String, V]] {

    def merge(base: ListMap[String, V], other: ListMap[String, V]): Result[ListMap[String, V]] = {
      (same(base) /: other) {
        case (merged, (otherKey, otherValue)) =>
          val res = base get otherKey match {
            case None => updated(otherKey -> otherValue, Nel.of(Path.empty -> s"new value for key $otherKey = $otherValue"))
            case Some(baseValue) => Merge[V].merge(baseValue, otherValue).map(otherKey -> _)
          }
          (merged |@| res.in(otherKey)).map { case (accMap, kv) => accMap  + kv }
      }
    }

  }

  implicit def listMapMergeListString[V:Merge]: Merge[ListMap[List[String], V]] = new Merge[ListMap[List[String], V]] {

    def merge(base: ListMap[List[String], V], other: ListMap[List[String], V]): Result[ListMap[List[String], V]] = {
      (same(base) /: other) {
        case (merged, (otherKey, otherValue)) =>
          val res = base get otherKey match {
            case None => updated(otherKey -> otherValue, Nel.of(Path.empty -> s"new value for key $otherKey = $otherValue"))
            case Some(baseValue) => Merge[V].merge(baseValue, otherValue).map(otherKey -> _)
          }
          (merged |@| res.in(otherKey)).map { case (accMap, kv) => accMap  + kv }
      }
    }

  }

}
