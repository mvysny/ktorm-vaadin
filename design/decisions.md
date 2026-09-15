# Decisions

Why this project is the way it is and not otherwise — FAQ-shaped: each entry is a question and
its current answer. Rewrite the answer when it changes; delete the entry when nobody asks any
more. An entry is earned by what it would cost to reverse — half the code base — or by research
the next person would otherwise redo (cited as its `R_`). Not an entry: windows → panels
"because that's the trend", this red over that red, `get_foo` over `is_foo?`, the testing library,
the CI host, a version bump — a comment at the site of the choice, or nothing; nothing about
`design/` itself. Cite by slug, `D_<slug>`, never by position; `grep '^## D_' design/decisions.md`
is the index. The first entry is the ruler: every later one trims to its length — which is how
long this file gets, so keep it short. When you have written an entry, re-read it against the one
above, check it says nothing the doc comments already say, and cut what is left over.

---

## D_single_global_database — Why one global Ktorm `Database` rather than an injected or per-request one?

`ActiveKtorm.database` is a `lateinit var` on an object, and `db { }` reads it to open every
transaction. Every public entry point is an extension on a Ktorm `Table` or `Column` —
`Employees.dataProvider`, `Employees.findAll()`, `Persons.id.e` — so there is no receiver to carry
a `Database` and no constructor to inject one into; threading it through would put a parameter on
every extension and on every component that builds a data provider. The global is what lets a
Vaadin component reach the database directly, which is the architecture this library is written
for. Why not a DI container: the library has no runtime of its own to hook one into, and
requiring Spring or CDI would rule out plain Vaadin Boot apps such as `testapp`. Why not scoping
to the Vaadin request: the request is not the unit of work — a Grid page and a form save each want
their own transaction, which `db { }` already gives them, and a Grid fetch triggered by a push or
a background thread has no request to scope to. The cost we carry: an app talking to two databases
cannot use any of these helpers, and every test must set the global before the first call —
`AbstractDbTest` does it in `@BeforeAll`.

## D_string_keyed_sorting — Why is a Grid column's sort key a string that has to match the Ktorm column?

Vaadin hands a data provider a `QuerySortOrder` carrying the Grid column's `key`, a `String`, and
nothing else — the data provider never sees the Grid, so there is no component to key a side map
by and no place to hang a Ktorm `Column`. The key therefore *is* the lookup, and the two providers
resolve it differently because they have different things to resolve against:
`EntityDataProvider` indexes its table, so the key is `Column.name`; `QueryDataProvider` has no
table — a select may carry joins, aliases and computed columns — so it walks the select expression
tree matching on `toString()`, and the key is `column.asExpression().toString()`. The `.e` and
`.q` extensions exist so that nobody types either string by hand. Why not our own `Grid` subclass
holding the mapping: it would make the library own the component, against **Glue, never a
framework**, and would do nothing for a `ComboBox` or a bare `DataProvider`. The cost we carry: a
wrong key fails at runtime rather than at compile time, so both providers throw with the
candidates listed — the column names, or the whole select expression.

## D_ilike — Why depend on `ktorm-support-postgresql` for `ilike` even on databases that are not Postgres?

Case-insensitive prefix matching is what `withStringFilterOn` and every `FilterTextField` example
does, and Ktorm core has no case-insensitive `like`. The one in `ktorm-support-postgresql` needs
no dialect installed and works on H2, where every test runs (`R_ilike_portability`), so the module
ships as an `api` dependency of the library and the helpers use it unconditionally. Why not
`lower(col) like lower(?)`, which is valid everywhere: it hides the column behind a function call,
so an ordinary index on it stops being usable and the consumer needs a functional index instead.
Why not make the operator pluggable: the two helpers that use it are a one-line `withStringFilter`
lambda each, so a consumer on another database writes their own faster than they would configure
ours. The cost we carry: on a database with no `ilike` keyword — MySQL and Oracle among them
(`R_ilike_portability`) — `withStringFilterOn` and the README's filter examples are a syntax
error, and the README says so at the point where a reader would otherwise copy them.
