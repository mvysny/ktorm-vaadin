# Ktorm for Vaadin — AGENTS.md

## What this is

[Ktorm](https://www.ktorm.org/) bindings for [Vaadin](https://vaadin.com/). Glues Ktorm entities to
Vaadin's UI primitives so you don't have to write the plumbing yourself. Ktorm owns the SQL DSL,
the entity model and the transaction; Vaadin owns the components. This project owns the glue: the
DataProviders that turn a Grid's paging, sorting and filtering into SQL, the name-based `Binder`
bridge that keeps JSR-303 working on interface entities, and filter components that produce Ktorm
`WHERE` fragments.

## Promises

- **You write the view, not the plumbing.** A Grid backed by a table pages, sorts and filters in SQL without a hand-written DataProvider.
- **Glue, never a framework.** We add no entity model, no query language and no filter DSL of our own; a user who knows Ktorm and Vaadin already knows this library.
- **Ktorm and Vaadin types at every boundary.** Filters are `ColumnDeclaring<Boolean>`, items are Ktorm entities, fields are Vaadin fields — nothing is wrapped in a project type the user must learn.

## Design docs

| File | Owns | Loaded |
|---|---|---|
| `README.md` | the pitch, install, how to use each feature | — |
| `AGENTS.md` (this) | promises, invariants, the module map, conventions, commands | every turn |
| `design/architecture.md` | how the pieces compose — wiring, dependency direction, the flows; normative | lazy |
| `design/decisions.md` | why this and not that — `D_` entries, FAQ-shaped | lazy |
| `design/research.md` | what Ktorm, Vaadin and Hibernate Validator actually do — `R_` entries with provenance | lazy |
| doc comments | what one symbol does and why it is shaped so | at the symbol |

Every fact lives in exactly one of these; the others link to it.

## Invariants

- **SQL this library issues goes through `db { }`.** Data providers, DAO helpers and `create()` open their transaction there; Ktorm's own `flushChanges()` and `delete()` use the `Database` the entity came from. See `D_single_global_database`.
- **`ActiveKtorm.database` is assigned once at bootstrap, before the first `db { }`.** It is a `lateinit` global; a `db { }` that runs first throws `UninitializedPropertyAccessException`.
- **A Grid column's key is the data provider's column key.** `Persons.id.e.key` for `EntityDataProvider`, `.q.key` for `QueryDataProvider`; any other key and sorting on it throws at runtime. See `D_string_keyed_sorting`.
- **`Binder` fields bind by property name, never by getter/setter lambdas.** A lambda binding never reaches `BeanValidationBinder`'s JSR-303 hook, so that field stops being validated. See `R_hv_interface_validation`.
- **JSR-303 annotations go on getters (`@get:NotNull`), not fields.** A Ktorm entity is an interface and has no field for the annotation to land on. See `R_hv_interface_validation`.

## Module map

- `ktorm-vaadin` — the library: data providers, the binder bridge, `ActiveEntity`, DAO helpers, filter components.
- `testapp` — runnable Vaadin Boot demo of the library; also the UI test bed.

## Conventions

- **Kotlin 2.4 on JDK 21, Gradle.** Dependency coordinates and versions live in `gradle/libs.versions.toml`; the root `build.gradle.kts` pins only the Gradle plugins.
- **Vaadin is `compileOnly`** in the library, `testImplementation` for tests — the consumer brings its own Vaadin version.
- **Tests: JUnit 5 plus Karibu-Testing** for UI assertions; a DB test extends `AbstractDbTest` (H2 in-memory + HikariCP).
- **A new feature gets a `testapp` route that uses it**, so the demo doubles as the integration test and the README's examples are copied from code that runs.
- **Case-insensitive matching is `ilike`** from `ktorm-support-postgresql`, re-exported as an `api` dependency. See `D_ilike`.
- **Public API carries KDoc**, with a usage example wherever the call is not obvious from the signature.

## Commands

- `./gradlew` — the default task, `clean build`: compiles both modules and runs all tests.
- `./gradlew :ktorm-vaadin:test --tests 'com.github.mvysny.ktormvaadin.EntityDataProviderTest'` — one test class or method.
- `./gradlew :testapp:run` — the demo on http://localhost:8080 (Vaadin Boot, embedded Jetty).
- `design/verify_design_tripwires.sh` — the doc-layer checks; also `./gradlew designTripwires`, which `check` depends on.
- CI runs `./gradlew --no-daemon --no-watch-fs` on JDK 21 and the tripwires in a job of their own (`.github/workflows/gradle.yml`).
- Release: tag, `publish closeAndReleaseStagingRepositories`, bump `-SNAPSHOT` — the steps are in `CONTRIBUTING.md`.

## Skills this project follows

- **Component-oriented:** self-sufficient components that reach the database directly, no MVC layers; the `cop` skill has the rules.
- **KDoc:** each fact at the level it belongs, nothing restating the signature; the `writing-kdoc` skill has the rules.

## Maintenance of this file

Loaded every turn; cap 34 KB, a module's own `AGENTS.md` 10 KB. Over it, in this order:
delete what has no home — status, history, class lists, what the code already says; trim
each line to its fact plus one clause and send the explanation home — why →
`design/decisions.md`, how across symbols → `design/architecture.md`, how in one symbol →
its doc comment, what upstream does → `design/research.md`; only then a module's own
`AGENTS.md`, peripheral modules first, never the core. Never paraphrase a lazy entry into a
line here. `design/verify_design_tripwires.sh` checks the caps and the cites.
