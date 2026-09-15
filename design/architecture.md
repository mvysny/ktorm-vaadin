# Architecture

How the pieces compose — what no single symbol can say and what would be expensive to overturn:
wiring and dependency direction, the lifecycle / threading / data-flow story, the flows a newcomer
needs, where to start reading; for a script deliverable, the inventory of the result and the steps
in the order they run. **Normative: the code conforms.** Change this file first, then the code.
Not here: why (`decisions.md` — cite the `D_`), what upstream does (`research.md` — cite the `R_`),
one symbol's behaviour (its doc comment), the module map (`AGENTS.md`). Only the sections with
content; the worked example in each is the ruler. Cap 12 KB — over it, research or doc-comment
content has crept in.

---

## Wiring

- Dependencies point toward the data and never back toward the UI: the library depends on Ktorm
  and, at compile time, on Vaadin. Nothing under `ktorm-vaadin/src/main/kotlin/` holds application
  state or knows about a route, a session or a `UI`.
- The seam to the database is `db { }` in `Transactions.kt`: it opens a Ktorm transaction on the
  `ActiveKtorm.database` global and hands the block a `KtormContext(transaction, database)`. Every
  data provider fetch and every DAO helper goes through it, so a test that wants a different
  database swaps the global and nothing else (`D_single_global_database`). Ktorm's own
  `flushChanges()` and `delete()` do not — they run on the `Database` the entity was loaded from,
  which is that same global.
- The library is a bag of extensions, not a graph of objects. A `Table<E>` gains `.dataProvider`
  and the DAO helpers; a `Column<*>` gains `.e` / `.q` for Grid keys; a `Binder.BindingBuilder`
  gains `.bind(column)` and `.toId(idColumn)`. Nothing is constructed at startup except the
  consumer's own `Database`.
- Vaadin is `compileOnly` here, so the library never pins a Vaadin version on its consumers; the
  price is that nothing in `src/main/` may rely on a Vaadin API newer than the floor the README
  states.
- Data flows one way through the filter components in `filter/`: each is a plain Vaadin field
  producing a plain value — an interval, a set of enum constants, a string. None of them knows a
  Ktorm column or a data provider; the caller turns the values into a `ColumnDeclaring<Boolean>`
  and calls `setFilter`. That is what lets one filter component serve a column, a join, or a
  hand-written expression.

## Flows

**A Grid page** (every scroll, and after every `refreshAll`):

1. Vaadin calls `sizeInBackEnd(query)` and then `fetchFromBackEnd(query)` on the data provider,
   handing it a `Query` with `offset`, `limit`, `sortOrders` and an optional per-query filter.
2. The provider ANDs that filter with the one `setFilter` stored, in `calculateFilter`.
3. Each `QuerySortOrder` is resolved back to a Ktorm expression by its string key —
   `table[key]` for `EntityDataProvider`, a walk of the select expression tree for
   `QueryDataProvider` (`D_string_keyed_sorting`).
4. `db { }` opens the transaction; the provider builds `from(table).select().where(…)
   .offset(…).limit(…).orderBy(…)`, or calls the consumer's query builder for
   `QueryDataProvider`.
5. Rows become items: `table.createEntity(row)` for entities, the consumer's `rowMapper` for a
   query. `sizeInBackEnd` runs the same shape with the SELECT rewritten to one `count(*)` column
   (`countQuery()`).

**A form save**:

1. `bind(binder).bind(column)` resolved the Ktorm column to a property *name* at form-build time,
   so the binding carries a `PropertyDefinition` and `BeanValidationBinder` attaches its JSR-303
   validator to it (`R_hv_interface_validation`). An entity-valued `ComboBox` gets
   `EntityToIdConverter` in front via `toId(idColumn)`.
2. `binder.writeBean(entity)` runs the field validators and the bean constraints, then writes into
   the Ktorm entity, which records the changes rather than applying them.
3. `entity.save()` validates once more through `ActiveKtorm.validator`, then picks its statement
   from the primary key: `flushChanges()` when the entity has one, an insert when it does not.
4. The insert path opens its own `db { }` through `Table.create`; `flushChanges()` goes straight
   to Ktorm. Validation runs before either, so a rejected entity writes nothing.

## Where to start reading

`Transactions.kt` — the one seam every other file goes through — then
`EntityDataProvider.kt`, which is the whole Vaadin-to-SQL translation in one class.
`testapp/src/main/kotlin/testapp/EmployeesRoute.kt` is the canonical consumer: a Grid, a join, a
filter bar and an edit dialog wired the way the README describes.
