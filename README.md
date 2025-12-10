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

TODO more
