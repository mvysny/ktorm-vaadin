package com.github.mvysny.ktormvaadin

import com.vaadin.flow.data.binder.Result
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

class ConvertersTest : AbstractDbTest() {
    @Nested
    inner class EntityToIdConverterTests {
        lateinit var person: Person

        @BeforeEach
        fun createPerson() {
            Persons.ddl()
            person = Person { name = "foo"; age = 25 }.save()
        }

        @AfterEach
        fun tearDownTestData() {
            db { ddl("drop table if exists person") }
        }

        val c = EntityToIdConverter(Persons.id, Person::class)

        @Test
        fun convertNullToIdReturnsNull() {
            expect(null) { c.convertToModel(null, null).value }
        }

        @Test
        fun convertNullIdReturnsNullEntity() {
            expect(null) { c.convertToPresentation(null, null) }
        }

        @Test
        fun convertEntityYieldsCorrectId() {
            expect(false) { person.id == null }
            expect(person.id) { c.convertToModel(person, null).value }
        }

        @Test
        fun convertToIdYieldsCorrectId() {
            expect(person) { c.convertToPresentation(person.id, null) }
        }
    }
}

val <T> Result<T>.value: T? get() = getOrThrow<RuntimeException> { RuntimeException(it) }