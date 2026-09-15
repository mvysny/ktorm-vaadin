# Ktorm for Vaadin

[![Gradle](https://github.com/mvysny/ktorm-vaadin/actions/workflows/gradle.yml/badge.svg)](https://github.com/mvysny/ktorm-vaadin/actions/workflows/gradle.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.mvysny.ktorm-vaadin/ktorm-vaadin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.github.mvysny.ktorm-vaadin/ktorm-vaadin)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[Ktorm](https://www.ktorm.org/) bindings for [Vaadin](https://vaadin.com/). Glues Ktorm entities to
Vaadin's UI primitives so you don't have to write the plumbing yourself. Ktorm owns the SQL DSL,
the entity model and the transaction; Vaadin owns the components. This project owns the glue: the
DataProviders that turn a Grid's paging, sorting and filtering into SQL, the name-based `Binder`
bridge that keeps JSR-303 working on interface entities, and filter components that produce Ktorm
`WHERE` fragments.

**Features:**

- Bind Ktorm entities to forms via Vaadin `Binder`, with JSR-303 validation that respects
  Ktorm's interface-based entities.
- Back a `Grid` or `ComboBox` with `EntityDataProvider` — pagination, sorting, and filtering
  are translated to SQL automatically.
- Drive Grids off arbitrary joins / projections with `QueryDataProvider`.
- Ready-made filter components (text, boolean, enum, date range, number range) that produce
  Ktorm `ColumnDeclaring<Boolean>` expressions.
- `ActiveEntity` mixin adds `save()`, `create()` and `validate()` to your entities; the
  table-level DAO helpers (`findAll`, `count`, `single`, `deleteAll`) work on any Ktorm `Entity`.

See the bundled `:testapp` (run with `./gradlew :testapp:run`) or the larger
[beverage-buddy-ktorm](https://github.com/mvysny/beverage-buddy-ktorm) example.

**Requirements:** JDK 21+, Kotlin 2.4+, Vaadin 25.2+, Ktorm 4.1+ (CI builds against Vaadin
25.2.6 only; other versions are untested). Licensed under [MIT](LICENSE).
Contributions and bug reports welcome — see [CONTRIBUTING.md](CONTRIBUTING.md).

## Contents

- [Adding to your project](#adding-to-your-project)
- [Entities and DAOs](#entities-and-daos)
- [Transactions and Active Entities](#transactions-and-active-entities)
- [EntityDataProvider](#entitydataprovider)
- [Joins via `QueryDataProvider`](#joins-via-querydataprovider)
- [Grid Sorting](#grid-sorting)
- [Grid Filters](#grid-filters)
- [Forms](#forms)
- [Other helpers](#other-helpers)
- [Further Documentation](#further-documentation)

## Adding to your project

First, add a dependency on ktorm-vaadin to your project:
```groovy
dependencies {
  implementation("com.github.mvysny.ktorm-vaadin:ktorm-vaadin:0.2")
}
```
ktorm-vaadin pulls in Ktorm and Hibernate-Validator for JSR-303 validation. Vaadin itself is a
`compileOnly` dependency — you bring your own version. You also want to add
[Hikari-CP](https://github.com/brettwooldridge/HikariCP) for connection pooling,
and [FlyWay](https://github.com/flyway/flyway) for keeping your database up-to-speed;
the [Forms](#forms) example below additionally uses
[Karibu-DSL](https://github.com/mvysny/karibu-dsl).

To initialize the database, we'll add the start/stop listener:
```kotlin
// Called by Jetty before the app starts serving requests, and afterwards when it's killed.
@WebListener
class Bootstrap : ServletContextListener {
    // Called by Jetty when the app starts up.
    override fun contextInitialized(servletContextEvent: ServletContextEvent?) {
        log.info("Connecting to the database")
        val cfg = HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
            username = "sa"
            password = ""
        }
        dataSource = HikariDataSource(cfg)
        ActiveKtorm.database = Database.connect(dataSource)
        log.info("Started")
    }

    // Called by Jetty when the app is stopped.
    override fun contextDestroyed(sce: ServletContextEvent?) {
        log.info("Closing database connections")
        dataSource.close()
        log.info("Destroyed")
    }

    companion object {
        private val log = LoggerFactory.getLogger(Bootstrap::class.java)
        private lateinit var dataSource: HikariDataSource
    }
}
```
A database connection is created and a Ktorm `Database` instance is
stored into `ActiveKtorm.database`. ktorm-vaadin assumes you connect to just one database
and uses it to make all database calls.

See `testapp`'s `Bootstrap.kt` file for a full example using FlyWay to create database structure.

## Entities and DAOs

Make sure to go through Ktorm documentation to learn how `Entity`-ies and `Table`s work.
We'll bind entities to forms via Vaadin Binder, and we'll return Entity instances via DataProvider,
so it's crucial that every table has an entity defined.

The entity is an interface and the `Table` object maps it to columns, exactly as Ktorm documents.
Extend `ActiveEntity<E>` rather than Ktorm's `Entity<E>` to get `save()`/`create()`/`validate()`;
that adds one obligation, the `table` property:

```kotlin
object Departments : Table<Department>("t_department") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val location = varchar("location").bindTo { it.location }
}

interface Department : ActiveEntity<Department> {
    val id: Int

    // the JSR-303 annotations go on the getter: an entity is an interface and has no field.
    @get:NotNull
    @get:Size(min = 1, max = 255)
    var name: String

    @get:NotNull
    var location: String

    override val table: Table<Department> get() = Departments

    // lets you write Department { name = "Marketing"; location = "Turku" }
    companion object : Entity.Factory<Department>()
}
```

These are ordinary Ktorm entities: `flushChanges()`, `delete()` and the `sequenceOf()` DSL keep
working as Ktorm documents them. See `testapp/src/main/kotlin/testapp/Entities.kt` for the full
set used throughout this README.

## Transactions and Active Entities

Every database call in ktorm-vaadin goes through the `db { }` block, which opens a Ktorm
transaction against `ActiveKtorm.database` and exposes a `KtormContext(transaction, database)`
to the block. The block commits on normal return and rolls back on exception:

```kotlin
val all: List<Employee> = db { database.sequenceOf(Employees).toList() }
```

For typical CRUD you don't need to call `db { }` directly — the `Table<E>` extension
helpers in `dao.kt` wrap it for you:

```kotlin
Employees.create(Employee { name = "Alice" })
Employees.findAll()      // List<Employee>
Employees.count()         // Int
Employees.single()        // single row, fails on 0 or 2+
Employees.deleteAll()
```

If your entities extend `ActiveEntity<E>` (instead of plain `Entity<E>`), each instance also
gets:

- `validate()` — runs `jakarta.validation` constraints. Annotate getters (`@get:NotNull`,
  `@get:Size(...)`) since Ktorm entities are interfaces.
- `isValid` — `validate()` wrapped in try/catch.
- `hasId` — true when the entity's primary key property is set.
- `save()` — calls `flushChanges()` if the entity's primary key property is set, otherwise
  inserts a new row. Pass `save(validate = false)` to skip the JSR-303 check.
- `create()` — always inserts.

`ActiveEntity` requires each entity to expose its `Table<E>` via the `table` property. **Hibernate
Validator 9+ is required** — HV 8 [doesn't run validators on interfaces](https://hibernate.atlassian.net/browse/HV-2018).

## EntityDataProvider

The `EntityDataProvider` provides instances of given entity.
To set the data provider to your Grid:

```kotlin
val dp: EntityDataProvider<Person> = Persons.dataProvider
// optionally set an unremovable filter, to always filter the records.
dp.setFilter(Persons.age gte 18)
grid.dataProvider = dp
```

To set the data provider to your ComboBox:

```kotlin
val dp: EntityDataProvider<Person> = Persons.dataProvider
// optionally set an unremovable filter, to always filter the records.
dp.setFilter(Persons.age gte 18)
comboBox.setItems(dp.withStringFilterOn(Persons.name))
```
## Joins via `QueryDataProvider`

When using [Ktorm Reference Bindings](https://www.ktorm.org/en/entity-finding.html#get-entities-by-sequences),
you can use `EntityDataProvider` to select one main entity and then reference all left-joined columns
in where clauses. Unfortunately the values of joined entities do not seem to be populated; for example when selecting `Employee`s
from Ktorm documentation, reading `Employee.department.name` will yield `null`. That's
where `QueryDataProvider` comes into play. (This is observed behavior rather than something Ktorm
documents; see `R_ktorm_joined_entity_values` in [design/research.md](design/research.md).)

To hold a left-join of `Employee` and `Department` (taken verbatim from
`testapp/src/main/kotlin/testapp/EmployeesRoute.kt`):
```kotlin
data class EmployeeDept(val employee: Employee, val department: Department) {
    companion object {
        fun from(row: QueryRowSet): EmployeeDept = EmployeeDept(
            Employees.createEntity(row), Departments.createEntity(row)
        )
        val dataProvider: QueryDataProvider<EmployeeDept> get() = QueryDataProvider(
            { it.from(Employees).leftJoin(Departments, on = Employees.departmentId eq Departments.id)
                .select(*Employees.columns.toTypedArray(), *Departments.columns.toTypedArray()) },
            { from(it) }
        )
    }
}
```

## Grid Sorting

You need to set the Grid Column key to the key its data provider expects — `.e.key`, the Ktorm
column name, for `EntityDataProvider`; `.q.key`, the rendered column expression, for
`QueryDataProvider`. That way we can reconstruct the Ktorm expression back from Vaadin's
QuerySortOrder:

```kotlin
// Pick `.e.key` when the Grid is backed by EntityDataProvider, `.q.key` for QueryDataProvider.
val idColumn = personGrid.addColumn { it.id }
        .setHeader("ID")
        .setSortable(true)
        .setKey(Persons.id.e.key) // or Persons.id.q.key for QueryDataProvider
dataProvider.setSortOrders(listOf(Persons.name.e.asc, Persons.age.e.asc))
```

## Grid Filters

One way of adding filters to your grid is to add a Grid header bar just for filter components,
then add filter components as cells to the header bar:

```kotlin
// append first header row: the column captions and the sorting indicator will appear here.
personGrid.appendHeaderRow()
// the second header row will host filter components.
val filterBar = personGrid.appendHeaderRow()

val nameFilter = FilterTextField()
val nameColumn = personGrid.addColumn { it.name }
        .setHeader("Name")
        .setSortable(true)
        .setKey(Persons.name.e.key)
filterBar.getCell(nameColumn).setComponent(nameFilter)
nameFilter.addValueChangeListener { updateFilter() }
```

Then, when any of the filter component
changes, you need to calculate the `ColumnDeclaring<?>` from the values of all filters as follows:

```kotlin
private fun update() {
    val conditions = mutableListOf<ColumnDeclaring<Boolean>?>()
    if (nameFilter.value.isNotBlank()) {
        conditions += Employees.name.ilike(nameFilter.value.trim() + "%")
    }
    if (jobFilter.value.isNotBlank()) {
        conditions += Employees.job.ilike(jobFilter.value.trim() + "%")
    }
    conditions += Employees.hireDate.between(hireDateFilter.value)
    conditions += Employees.salary.between(salaryFilter.value.asLongInterval())
    dataProvider.setFilter(conditions.and())
}
```

Alternatively, you might have a `FilterBean` populated by the dialog. Whenever the "Apply" button
of the dialog is pressed, you populate the FilterBean from the components; you can then
calculate the `ColumnDeclaring` from the bean in a similar way as above.

See the bundled `testapp` example
project for more details.

This project offers additional filter components:

* `BooleanFilterField`: allows the user to select `true` or `false` or clear the selection and disable this filter.
* `EnumFilterField`: allows the user to select one or more enum constants. If all constants or no constant is selected, the filter is disabled.
* `FilterTextField`: a simple TextField filter. Usually matched with a column using the `ilike` operator.
* `DateRangePopup`: allows the user to select a date range. The range may be open (only the 'from' or 'to' date filled in, but not both). Usually matched using the `between()` operator.
* `NumberRangePopup`: allows the user to select a numeric range. The range may be open (only the 'from' or 'to' number filled in, but not both). Usually matched using the `between()` operator.

`BooleanFilterField` and `EnumFilterField` have no `between()` helper since they map
straight onto `eq`/`inList`; both express "don't filter" as a value you must check for:

```kotlin
// an empty selection means "don't filter"; so does a selection of every constant.
if (!maritalStatusFilter.isAllOrNothingSelected) {
    conditions += Employees.maritalStatus.inList(maritalStatusFilter.selectedItems.toList())
}
val remote = remoteFilter.value
if (remote != null) {
    conditions += Employees.remote eq remote
}
```

> **Note on `ilike`:** the `withStringFilterOn(...)` helpers and several filter examples use
> `org.ktorm.support.postgresql.ilike`. `ktorm-vaadin` pulls `ktorm-support-postgresql` in
> transitively so the operator is always available — H2 understands `ilike` too, which is
> why tests pass. If you target a database that doesn't (e.g. MySQL), substitute your own
> case-insensitive comparison when composing the filter expression.

## Forms

Ktorm entities are interfaces, but have all the usual getters/setters so that they work
with the Binder. However, make sure to have Hibernate-Validator 9+ since HV 8
[doesn't run validators on interfaces](https://hibernate.atlassian.net/browse/HV-2018).

The form example below uses two helpers from [Karibu-DSL](https://github.com/mvysny/karibu-dsl)
(not from ktorm-vaadin):

- `beanValidationBinder<T>()` — a thin wrapper around Vaadin's `BeanValidationBinder<T>`.
- `HasBinder<T>` — marker interface that lets `bind(binder)` find the binder from the
  enclosing form. Implement it on your form class (as below) or pass the binder explicitly.

The `bind(...)` and `toId(...)` calls inside the form are provided by ktorm-vaadin — see
`binder.kt`. `bind(column)` binds the field to the entity property by **name** (rather than
getter/setter lambdas) so that BeanValidationBinder can run JSR-303 against it; `toId(idColumn)`
adapts an entity-valued field to a foreign-key `ID?` column.

A very simple example of an employee form:

```kotlin

class EmployeeForm : FormLayout(), HasBinder<Employee> {
    override val binder = beanValidationBinder<Employee>()
    init {
        textField("Name") {
            setId("name")
            bind(binder).bind(Employees.name)
        }
        textField("Job") {
            setId("job")
            bind(binder).bind(Employees.job)
        }
        comboBox<Employee>("Manager") {
            setId("manager")
            setItems(Employees.dataProvider.withStringFilterOn(Employees.name))
            itemLabelGenerator = ItemLabelGenerator { it.name }
            bind(binder).toId(Employees.id).bind(Employees.managerId)
        }
        datePicker("Hire Date") {
            setId("hireDate")
            bind(binder).bind(Employees.hireDate)
        }
        integerField("Salary") {
            setId("salary")
            bind(binder).withConverter(IntegerToLongConverter()).bind(Employees.salary)
        }
        comboBox<Department>("Department") {
            setId("department")
            setItems(Departments.dataProvider.withStringFilterOn(Departments.name))
            itemLabelGenerator = ItemLabelGenerator { it.name }
            bind(binder).toId(Departments.id).bind(Employees.departmentId)
        }
    }
}
```
Notice how the Manager and Department ComboBoxes populate themselves, and how they
bind to an `Int` field which holds the ID of the manager/department.

## Other helpers

* **Raw SQL inside `db { }`**: `ddl()` runs a DDL or update statement, `sql()` runs a query and
  maps every row. Both use the transaction's JDBC connection, so guard against SQL injection
  yourself:
  ```kotlin
  db {
      ddl("create table if not exists audit(id int primary key, msg varchar)")
      val total: Long = sql("select sum(salary) from t_employee where job = ?",
          { setString(1, "Manager") }) { it.getLong(1) } .firstOrNull() ?: 0L
  }
  ```
* **`Collection<ColumnDeclaring<Boolean>?>.and()`**: ANDs a list of conditions, dropping the nulls
  and returning `null` when nothing remains — that's what the filter examples above call.
* **`withStringFilter { }`**: `withStringFilterOn(column)` for the cases where you build the where
  clause yourself, e.g. to search two columns at once:
  ```kotlin
  setItems(Employees.dataProvider.withStringFilter {
      Employees.name.ilike("$it%") or Employees.job.ilike("$it%")
  })
  ```
* **`ActiveKtorm.validator`**: swap in your own `jakarta.validation.Validator`.
* Every filter component takes an optional id (`FilterTextField("name_filter")`), which is handy
  in UI tests; `EnumFilterField` takes the enum class as well:
  `EnumFilterField(MaritalStatus::class.java, "marital_status_filter")`.

## Further Documentation

This project contains a bundled app named `testapp`. You can run it easily:
```bash
$ ./gradlew testapp:run
```
The sources are simple and easy to follow and demo all features of ktorm-vaadin.

For development setup, running tests, and release procedure see [CONTRIBUTING.md](CONTRIBUTING.md).
Underlying APIs are documented in the [Ktorm docs](https://www.ktorm.org/) and the
[Vaadin Binder docs](https://vaadin.com/docs/latest/flow/binding-data/components-binder).

If you're changing ktorm-vaadin itself, the `design/` folder explains it:

* [design/architecture.md](design/architecture.md) — how the pieces compose: the wiring, the
  dependency direction, the flows.
* [design/decisions.md](design/decisions.md) — why this and not that, e.g. why one global
  `Database`, or why `ilike` comes from `ktorm-support-postgresql`.
* [design/research.md](design/research.md) — what Ktorm, Vaadin and Hibernate Validator actually
  do, each finding with its provenance.
