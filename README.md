Consolidate Scala library
=========================

This library offers consolidate of immutable data, through the `Merge[A]` typeclass (which
can be autoderived with optional validation) and the `Result[A]` result type.

After merging a current piece of data (`lhs`) with a new piece `rhs` using `lhs merge rhs`,
the result is:

- `Same(lhs)` if `rhs` does not offer additional information,
- `Update(m, updates)` if `rhs` has new information, with `m` the merged result and `updates` is a
  non-empty list of changes,
- `Failed(errors)` if `lhs` and `rhs` are incompatible, and `errors` is a non-empty list of errors.

`Merge[A]` is an `ApplicativeError`, which accumulates updates and errors. 

The default Merge typeclass implementation is `Merge.fromEquals`, that compares `lhs`
and `rhs` and returns `Same(lhs)` when `lhs == rhs` and `Failed(errors)` otherwise.

Implementation are provided for `Map[String, V]` (by completing the map entries, and merging the
values when both `lhs` and `rhs` provide a key), `Option` (by taking the `Some` entry,
and merging both `Some` when applicable), `Set` (by taking the union of sets).
