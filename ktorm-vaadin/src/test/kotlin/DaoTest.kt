package com.github.mvysny.ktormvaadin

import com.github.mvysny.kaributesting.v10.expectList
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ktorm.entity.count
import org.ktorm.entity.sequenceOf
import kotlin.test.expect

class DaoTest : AbstractDbTest() {
    @BeforeEach
    fun prepareTestData() {
        Persons.ddl()
    }

    @AfterEach
    fun tearDownTestData() {
        db { ddl("drop table if exists person") }
    }

    @Test
    fun create() {
        Persons.create(Person { name = "Jon"; age = 25 })
        expect(1) { db { database.sequenceOf(Persons).count() } }
    }

    @Test
    fun count() {
        expect(0) { Persons.count() }
        Persons.create(Person { name = "Jon"; age = 25 })
        expect(1) { Persons.count() }
    }
    @Test
    fun listAll() {
        val p = Persons.create(Person { name = "Jon"; age = 25 })
        expectList(p) { Persons.findAll() }
    }
    @Test
    fun single() {
        assertThrows<RuntimeException> { Persons.single() }
        val p = Persons.create(Person { name = "Jon"; age = 25 })
        expect(p) { Persons.single() }
    }
    @Test
    fun deleteAll() {
        Persons.create(Person { name = "Jon"; age = 25 })
        Persons.deleteAll()
        expect(0) { Persons.count() }
    }
}