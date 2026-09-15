package com.github.mvysny.ktormvaadin

import jakarta.validation.ConstraintViolationException
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ktorm.entity.Entity
import org.ktorm.entity.count
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import kotlin.test.expect

class ActiveEntityTest : AbstractDbTest() {
    @BeforeEach fun prepareTestData() {
        Persons.ddl()
    }

    @AfterEach fun tearDownTestData() {
        db { ddl("drop table if exists person") }
    }

    @Test fun validate() {
        expect(false) { Person {}.isValid }
        expect(false) { Person { name = "foo" }.isValid }
        expect(false) { Person { name = "foo"; age = -5 }.isValid }
        expect(true) { Person { name = "foo"; age = 5 }.isValid }
    }

    @Test fun validateThrowsAndNamesTheOffendingProperties() {
        val ex = assertThrows<ConstraintViolationException> { Person { name = "foo" }.validate() }
        expect(setOf("age")) { ex.constraintViolations.map { it.propertyPath.toString() } .toSet() }
        Person { name = "foo"; age = 5 }.validate()
    }

    @Test fun saveValidatesByDefault() {
        val p = Person { name = "foo"; age = -5 }
        assertThrows<ConstraintViolationException> { p.save() }
        expect(0) { Persons.count() }
    }

    @Test fun saveCanSkipValidation() {
        Person { name = "foo"; age = -5 }.save(false)
        expect(-5) { Persons.single().age }
    }

    @Test fun createValidatesByDefault() {
        val p = Person { name = "foo"; age = -5 }
        assertThrows<ConstraintViolationException> { p.create() }
        expect(0) { Persons.count() }
    }

    @Test fun createCanSkipValidation() {
        Person { name = "foo"; age = -5 }.create(false)
        expect(-5) { Persons.single().age }
    }

    @Test fun hasIdFailsOnTableWithoutPrimaryKey() {
        val ex = assertThrows<IllegalStateException> { LogEntry { message = "foo" }.hasId }
        expect(true, ex.message) { ex.message!!.contains("Primary keys cannot be empty") }
    }

    /**
     * Tests [property], which maps a Ktorm column back to the entity property it is bound to.
     */
    @Nested inner class columnPropertyTests {
        @Test fun nestedBindingYieldsTheBoundProperty() {
            expect("name") { Persons.name.property.name }
        }

        @Test fun referenceBindingYieldsTheReferencingProperty() {
            expect("team") { Members.teamId.property.name }
        }

        @Test fun deeplyNestedBindingIsRejected() {
            val ex = assertThrows<IllegalArgumentException> { Members.teamName.property }
            expect(true, ex.message) { ex.message!!.contains("nested properties aren't supported") }
        }
    }

    @Test fun testHasId() {
        val p = Person { name = "foo"; age = 10 }
        expect(false) { p.hasId }
        p.create()
        expect(false) { p.id == null }
        expect(true) { p.hasId }
    }

    @Test fun saveCreates() {
        val p = Person { name = "foo"; age = 10 }
        p.save()
        expect(true) { p.id != null }
        expect(1) { db { database.sequenceOf(Persons).count() } }
    }

    @Test fun saveUpdates() {
        val p = Person { name = "foo"; age = 10 }
        p.create()
        val id = p.id
        expect(true) { id != null }
        expect(1) { db { database.sequenceOf(Persons).count() } }
        p.name = "bar"
        p.save()
        expect(id) { p.id }
        expect(1) { db { database.sequenceOf(Persons).count() } }
    }
}

object Persons : Table<Person>("person") {
    // important: keep the property name "id" lowercase while keeping the column name uppercase.
    // This tests that EntityToIdConverter uses the right naming for value retrieval.
    val id = int("ID").primaryKey().bindTo { it.id }
    val name = varchar("NAME").bindTo { it.name }
    val age = int("AGE").bindTo { it.age }
    fun ddl() {
        db { ddl("create table person (id int not null primary key auto_increment, name varchar(255) not null, age int not null)") }
    }
}

/**
 * Never created in the database: only the column bindings are tested.
 */
object Logs : Table<LogEntry>("log") {
    val message = varchar("MESSAGE").bindTo { it.message }
}

interface LogEntry : ActiveEntity<LogEntry> {
    var message: String?

    override val table: Table<LogEntry> get() = Logs
    companion object : Entity.Factory<LogEntry>()
}

object Teams : Table<Team>("team") {
    val id = int("ID").primaryKey().bindTo { it.id }
    val name = varchar("NAME").bindTo { it.name }
}

interface Team : Entity<Team> {
    val id: Int?
    var name: String?
    companion object : Entity.Factory<Team>()
}

/**
 * Never created in the database: only the column bindings are tested.
 */
object Members : Table<Member>("member") {
    val id = int("ID").primaryKey().bindTo { it.id }
    val teamId = int("TEAM_ID").references(Teams) { it.team }
    val teamName = varchar("TEAM_NAME").bindTo { it.team.name }
}

interface Member : Entity<Member> {
    val id: Int?
    var team: Team
    companion object : Entity.Factory<Member>()
}

interface Person : ActiveEntity<Person> {
    val id: Int?

    @get:NotNull
    @get:NotBlank
    @get:Size(min = 1, max = 255)
    var name: String?

    @get:NotNull
    @get:Min(0)
    @get:Max(100)
    var age: Int?

    override val table: Table<Person>
        get() = Persons
    companion object : Entity.Factory<Person>()
}