# Research — the stack we bind: Ktorm 4.1, Vaadin 25.2, Hibernate Validator 9

What the things we don't own actually do. About *them*, never us: a sentence starting "we chose"
is a `D_`. `## R_<slug> — <title>`, one claim per bullet, one provenance marker per claim —
**[docs]**, **[src]**, **[verified <date>, <version>]**, **[unverified]** (a hypothesis; a design
built on it says so). A claim is earned by its provenance, or by having cost real work to find
out. Checked against the versions pinned in `gradle/libs.versions.toml`; a version-sensitive claim
names the version it was seen on. Cite by slug, `R_<slug>`, never by position;
`grep '^## R_' design/research.md` is the index. The first entry is the ruler: every later one
trims to its length — which is how long this file gets, so keep it short. When you have written an
entry, re-read it against the one above and cut what the provenance markers already carry.

---

## R_hv_interface_validation — JSR-303 on interface-typed beans: Hibernate Validator and Vaadin's Binder

- HV 9 runs constraints declared on the getters of an interface-typed bean — which is what a Ktorm
  entity is — both through `Validator.validate` and through `BeanValidationBinder`.
  **[verified 2026-09-15, hibernate-validator 9.1.0.Final]**, `ActiveEntityTest`,
  `BinderTests.validationWorks`
- HV 8 did not, which is where the 9+ floor comes from. Attributed to Hibernate
  [HV-2018](https://hibernate.atlassian.net/browse/HV-2018); that page renders client-side and
  could not be read for this entry, and nothing here runs against HV 8. **[unverified]**
- An annotation with no use-site target, or `@field:`, has no field to land on — an interface
  declares none — so it is unlikely to be what the validator reads. No test asserts it either way.
  **[unverified]**
- `BeanValidationBinder` attaches its JSR-303 validator in `configureBinding(BindingBuilder,
  PropertyDefinition)`, which Vaadin calls only for a binding that has a `PropertyDefinition` —
  that is, one made through `bind(HasValue, String)`. A binding made with
  `bind(ValueProvider, Setter)` never reaches it, and is then validated by whatever `withValidator`
  calls it was given and nothing else. **[docs]**, Vaadin 25.2.7 javadoc

## R_ilike_portability — `org.ktorm.support.postgresql.ilike` outside Postgres

- `ilike` is an ordinary Ktorm expression that renders to the SQL keyword `ilike`; it needs no
  `PostgreSqlDialect` installed. Neither `AbstractDbTest` nor `testapp`'s `Bootstrap` passes a
  dialect to `Database.connect`, and the filter tests still produce working SQL.
  **[verified 2026-09-15, ktorm 4.1.1]**
- H2 accepts `ilike` and matches case-insensitively, in its default (non-compatibility) mode —
  which is why the whole filter suite passes on an in-memory H2.
  **[verified 2026-09-15, H2 2.4.240]**, `EmployeesRouteTest.testNameFilter`
- MySQL and Oracle have no `ilike` keyword, so the same expression is a syntax error there rather
  than a silently case-sensitive match. Not run against either. **[unverified]**

## R_ktorm_joined_entity_values — Ktorm reference bindings and left-joined columns

- With a reference binding (`references(Departments) { it.department }`), selecting the main entity
  and left-joining the referenced table lets the join's columns be used in `where` clauses, but the
  referenced entity comes back unpopulated — `Employee.department.name` reads as `null` after
  `Employees.createEntity(row)`. Stated in this project's README as the reason `QueryDataProvider`
  exists; every table here binds with `bindTo` rather than `references`, so nothing reproduces it,
  and it is not traced to a line in Ktorm's sources or to an upstream issue. **[unverified]**
