package com.github.mvysny.ktormvaadin

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktorm.entity.Entity
import org.ktorm.entity.count
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import kotlin.test.expect

class ActiveEntityTest : AbstractDbTest() {
    @BeforeEach fun prepareTestData() {
        db { ddl("create table person (id int not null primary key auto_increment, name varchar(255) not null, age int not null)") }
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
    val id = int("ID").primaryKey().bindTo { it.id }
    val name = varchar("NAME").bindTo { it.name }
    val age = int("AGE").bindTo { it.age }
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