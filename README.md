# ktorm-vaadin

[Ktorm](https://www.ktorm.org/) bindings for [Vaadin](https://vaadin.com/). Provides support for binding Ktorm entities to
forms via Vaadin binder, listing them in Grids and ComboBoxes via DataProvider,
and listing the outcomes of SQL selects/joins in a Grid.

> Note: work in progress. The code will be ported from the example app [beverage-buddy-ktorm](https://github.com/mvysny/beverage-buddy-ktorm).

## Adding to your project

First, add a dependency on ktorm-vaadin to your project:
```groovy
dependencies {
  implementation("com.github.mvysny.ktorm-vaadin:ktorm-vaadin:0.1")
}
```
ktorm-vaadin pulls in Ktorm, Hibernate-Validator for JSR-303 validation, but you
also want to add [Hikari-CP](https://github.com/brettwooldridge/HikariCP) for connection pooling,
and [FlyWay](https://github.com/flyway/flyway) for keeping your database up-to-speed.

To initialize the database, we'll add the start/stop listener:
```kotlin
// Called by Jetty before the app starts serving requests, and afterwards when it's killed.
@WebListener
class Bootstrap : ServletContextListener {
    // Called by Jetty when the app starts up.
    @Override
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
        @JvmStatic
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
comboBox.setDataProvider(dp.withStringFilterOn(Persons.name))
```
## Joins via `QueryDataProvider`

When using [Ktorm Reference Bindings](https://www.ktorm.org/en/entity-finding.html#get-entities-by-sequences),
you can use `EntityDataProvider` to select one main entity and then reference all left-joined columns
in where clauses. Unfortunately the values of joined entities do not seem to be populated; for example when selecting `Employee`s
from Ktorm documentation, reading `Employee.department.name` will yield `null`. That's
where `QueryDataProvider` comes into play.

To hold a join of `Person` and `Address`:
```kotlin
data class PersonAddress(val person: Person, val address: Address) {
    companion object {
        fun from(row: QueryRowSet): PersonAddress = PersonAddress(
            Persons.createEntity(row), Addresses.createEntity(row)
        )
        val dataProvider: QueryDataProvider<PersonAddress> get() = QueryDataProvider(
            { it.from(Addresses).leftJoin(Persons, on = Addresses.of_person_id eq Persons.id) },
            { it.select(*Addresses.columns.toTypedArray(), *Persons.columns.toTypedArray())},
            { from(it) }
        )
    }
    override fun toString(): String = "${person.name}/${person.age}=${address.street}/${address.city}"
}
```

## Grid Sorting

You need to set the Grid Column key to the Ktorm Column name; that way we can reconstruct
the Ktorm expression back from Vaadin's QuerySortOrder:

```kotlin
val idColumn = personGrid.addColumn { it.id }
        .setHeader("ID")
        .setSortable(true)
        .setKey(Persons.id.e.key) // When using EntityDataProvider
    .setKey(Persons.id.q.key) // When using QueryDataProvider
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
* `FilterTextField`: a simple TextField filter. Usually matched with a column using the `likeIgnoreCase()` operator.
* `DateRangePopup`: allows the user to select a date range. The range may be open (only the 'from' or 'to' date filled in, but not both). Usually matched using the `between()` operator.
* `NumberRangePopup`: allows the user to select a numeric range. The range may be open (only the 'from' or 'to' number filled in, but not both). Usually matched using the `between()` operator.

## Forms

Ktorm entities are interfaces, but have all the usual getters/setters so that they work
with the Binder. However, make sure to have Hibernate-Validator 9+ since HV 8
[doesn't run validators on interfaces](https://hibernate.atlassian.net/browse/HV-2018).

- TODO how to implement a Department picker ComboBox and bind it to an ID field.
- TODO verify that left-joined fields aren't populated

## Further Documentation

This project contains a bundled app named `testapp`. You can run it easily:
```bash
$ ./gradlew testapp:run
```
The sources are simple and easy to follow and demo all features of ktorm-vaadin.
