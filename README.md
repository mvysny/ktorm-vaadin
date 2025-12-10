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

To initialize the database:
