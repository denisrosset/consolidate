Consolidate Scala library
=========================

This library offers consolidate of immutable data, through the `Merge[A]` typeclass and
the `Merged[A]` result.

After merging a current piece of data (`lhs`) with a new piece `rhs` using `lhs merge rhs`,
the result is:

- `MSame(lhs)` if `rhs` does not offer additional information,
- `MNew(m)` if `rhs` has new information, with `m` the merged result,
- `MFail(log)` if `lhs` and `rhs` are incompatible, with `log: MLog` a list of
  encountered errors.

`Merge[A]` is a monad, with the following operations:

- `unit(a: A) = MSame(a)`,
- `map[A,B](m: Merged[A])(f: A => B): Merged[B]` with
  `map(MSame(a))(f) = MSame(f(a))`
  `map(MNew(a))(f) = MNew(f(a))`
  `map(MFail(log))(f) = MFail(log)`
- `flatMap[A,B](m: Merged[A])(f: A => Merged[B]): Merged[B]` with
  `flatMap(MSame(a))(f) = f(a)`
  `flatMap(MNew(a))(f) = MNew(f(a)) if f(a) == MSame(a) or MNew(a)`
  `flatMap(MFail(log))(f) = MFail(log)`

The default Merge typeclass implementation is `Merge.fromEquals`, that compares `lhs`
and `rhs` and returns `MSame(lhs)` when `lhs == rhs` and `MFail(log)` otherwise.

Implementation are provided for `Map` (by completing the map entries, and merging the
values when both `lhs` and `rhs` provide a key), `Option` (by taking the `Some` entry,
and merging both `Some` when applicable), `Set` (by taking the union of sets).
