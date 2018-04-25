package com.faacets.consolidate

import cats.data.{NonEmptyList => NEL, Validated, ValidatedNel}
import cats.syntax.all._
import shapeless._

import Result.{Same, Updated, Failed}

object Auto extends LabelledTypeClassCompanion[Merge] {

  class Want[T](val u: Unit) {

    def noValidation[LKV](implicit
      lgen: LabelledGeneric.Aux[T, LKV],
      lwclkv: Lazy[Wrap[LKV]]
    ): Merge[T] = deriveInstance[T, LKV]

    def validated[LKV, V, G](validate: G => ValidatedNel[String, T])(implicit
      lgen: LabelledGeneric.Aux[T, LKV],
      gen: Generic.Aux[G, V],
      lwclkv: Lazy[Wrap.Aux[LKV, V]]
    ): Merge[T] = new Merge[T] {

      import lwclkv.value.{label, unlabel, unwrap}

      protected def toFailed(errors: NEL[String]): Result[T] =
        Failed(errors.map(err => (Path.empty, err)))

      def merge(f1: T, f2: T) = unwrap.merge(unlabel(lgen.to(f1)), unlabel(lgen.to(f2))) match {
        case Same(v) => validate(gen.from(v)) match {
          case Validated.Valid(f) => Same(f)
          case Validated.Invalid(errors) => toFailed(errors)
        }
        case Updated(v, updates) => validate(gen.from(v)) match {
          case Validated.Valid(f) => Updated(f, updates)
          case Validated.Invalid(errors) => toFailed(errors)
        }
        case failed: Failed => failed
      }
    }

  }

  def derive[T]: Want[T] = new Want[T]( () )

  object typeClass extends LabelledTypeClass[Merge] {
    def emptyProduct = new Merge[HNil] {
      def merge(t1: HNil, t2: HNil) = Same(HNil)
    }

    def product[F, T <: HList](name: String, mh: Merge[F], mt: Merge[T]) = new Merge[F :: T] {
      def merge(ft1: F :: T, ft2: F :: T) = {
        val head = mh.merge(ft1.head, ft2.head).in(name)
        val tail = mt.merge(ft1.tail, ft2.tail)
        (head, tail).mapN(_ :: _)
      }
    }

    def emptyCoproduct = new Merge[CNil] {
      def merge(t1: CNil, t2: CNil) = Same(t1.impossible)
    }

    def coproduct[L, R <: Coproduct](name: String, sl: => Merge[L], sr: => Merge[R]) = new Merge[L :+: R] {
      def merge(lr1: L :+: R, lr2: L :+: R) = (lr1, lr2) match {
        case (Inl(l1), Inl(l2)) => sl.merge(l1, l2).map(Coproduct[L :+: R](_))
        case (Inr(r1), Inr(r2)) => sr.merge(r1, r2).map(_.extendLeftBy[L :+: CNil])
        case (Inl(l1), Inr(r2)) => Failed(NEL.of(Path.empty -> s"types differ between $l1 and $r2"))
        case (Inr(r1), Inl(l2)) => Failed(NEL.of(Path.empty -> s"types differ between $l2 and $r1"))
      }
    }

    def project[F, G](instance: => Merge[G], to: F => G, from: G => F) = new Merge[F] {
      def merge(f1: F, f2: F) = instance.merge(to(f1), to(f2)).map(from)
    }

  }

}
