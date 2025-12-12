package com.github.mvysny.ktormvaadin

import com.vaadin.flow.data.binder.Result
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

class ConvertersTest : AbstractDbTest() {
    @Nested
    inner class EntityToIdConverterTests {
        val c = EntityToIdConverter(Persons.id, Person::class)
        @Test
        fun convertNullToIdReturnsNull() {
            expect(null) { c.convertToModel(null, null).value }
        }
        @Test
        fun convertNullIdReturnsNullEntity() {
            expect(null) { c.convertToPresentation(null, null) }
        }
    }
}

val <T> Result<T>.value: T? get() = getOrThrow<RuntimeException> { RuntimeException(it) }