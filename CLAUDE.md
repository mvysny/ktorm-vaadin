# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Ktorm bindings for Vaadin. A Kotlin library (`:ktorm-vaadin`) that glues Ktorm entities to Vaadin UI primitives (Binder, Grid/ComboBox DataProviders, filter components), plus a bundled `:testapp` that exercises every feature via a runnable Vaadin Boot app.

- Kotlin 2.3, JDK 21, Gradle.
- Published to Maven Central as `com.github.mvysny.ktorm-vaadin:ktorm-vaadin`.
- Version & dep coordinates are centralized in `gradle/libs.versions.toml`.

## Commands

Build + all tests (default task is `clean build`):
```
./gradlew
```

Run just the library's tests:
```
./gradlew :ktorm-vaadin:test
```

Run a single test class / method (JUnit):
```
./gradlew :ktorm-vaadin:test --tests 'com.github.mvysny.ktormvaadin.EntityDataProviderTest'
./gradlew :ktorm-vaadin:test --tests 'com.github.mvysny.ktormvaadin.EntityDataProviderTest.someMethod'
```

Run the demo app (Vaadin Boot, embedded Jetty on http://localhost:8080):
```
./gradlew :testapp:run
```

Release flow is documented in `CONTRIBUTING.md` (tag, `publish closeAndReleaseStagingRepositories`, bump `-SNAPSHOT`).

## Architecture

The library assumes **a single global Ktorm `Database`** held in `ActiveKtorm.database`. Every public API call goes through `db { ... }` (`Transactions.kt`), which opens a Ktorm transaction and exposes `KtormContext(transaction, database)` to the block. There is no per-request scoping; all code runs against that one DataSource. Tests bootstrap it via `AbstractDbTest` (H2 in-memory + HikariCP); the demo app does the same in `testapp/Bootstrap.kt` plus Flyway migrations.

Core pieces under `ktorm-vaadin/src/main/kotlin/`:

- **`EntityDataProvider<T>`** — Vaadin `ConfigurableFilterDataProvider` that selects rows from a single Ktorm `Table<T>` and materializes them via `table.createEntity(row)`. Filter type is `ColumnDeclaring<Boolean>` (i.e. a Ktorm WHERE fragment), not a POJO. Grid sorting works by looking up the column by its `Column.name` — **the Vaadin Grid column's `key` must equal the Ktorm column name**. Use the `Column<*>.e` extension to get that key (`Persons.id.e.key`, `.asc`, `.desc`).
- **`QueryDataProvider<T>`** — for joins / arbitrary selects. Takes a `(Database) -> Query` builder + a `(QueryRowSet) -> T` row mapper. Sorting reconstructs the Ktorm expression by walking the SQL expression tree and matching on `toString()` — use `Column<*>.q.key` (which is `column.asExpression().toString()`) as the Grid column key so lookups succeed. `countQuery()` rewrites the SELECT to a single `count(*)` column for `sizeInBackEnd`.
- **`ActiveEntity<E>`** — extends Ktorm `Entity<E>` with `validate()` (jakarta.validation via `ActiveKtorm.validator`), `save()` / `create()` that picks insert-vs-update based on primary-key presence, and requires each entity to expose its `Table<E>`. JSR-303 annotations must go on getters (`@get:NotNull`) because Ktorm entities are interfaces — and Hibernate Validator **9+** is required (HV 8 doesn't validate interfaces, see HV-2018).
- **`binder.kt`** — `Binder.BindingBuilder.bind(column)` binds a Vaadin field to a Ktorm `Column` by property **name** (not getter/setter lambdas — named binding is what makes BeanValidationBinder run JSR-303). Handles Kotlin's `isFoo` → `foo` property-name rewrite. `toId(idColumn)` wraps an entity-valued field so it binds to a foreign-key `ID?` column (see `Converters.kt` → `EntityToIdConverter`).
- **`dao.kt`** — table-level extension fns (`create`, `findAll`, `single`, `deleteAll`, `count`) wrapping `sequenceOf` in `db { }`.
- **`filter/`** — reusable filter components (`FilterTextField`, `BooleanFilterField`, `EnumFilterField`, `DateRangePopup`, `NumberRangePopup`) and their value types (`NumberInterval`, `DateInterval`, `ClosedInterval`). They produce values; the caller is responsible for translating them into a `ColumnDeclaring<Boolean>` and calling `dataProvider.setFilter(...)`. See `testapp/EmployeesRoute.kt` for the canonical wiring pattern.
- **`utils.kt`** — `Collection<ColumnDeclaring<Boolean>?>.and()` null-safe reducer used when composing filter conditions.

### `ilike`

`EntityDataProvider.withStringFilterOn` / `QueryDataProvider.withStringFilterOn` use `org.ktorm.support.postgresql.ilike`. The `ktorm-support-postgresql` dependency is pulled in for this operator even when running on non-Postgres (tests use H2) — H2 understands `ilike` so this happens to work for tests, but consumers on other DBs may need to substitute their own case-insensitive comparison.

### Testing

Tests use JUnit (`useJUnitPlatform()`) + Karibu-Testing for UI assertions. DB tests extend `AbstractDbTest` which stands up H2 in-memory and points `ActiveKtorm.database` at it in `@BeforeAll`. Tests for filters/UI live under `ktorm-vaadin/src/test/kotlin/{filter,utils}` and `testapp/src/test/kotlin/testapp`.
