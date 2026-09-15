package com.github.mvysny.ktormvaadin.filter

import com.github.mvysny.kaributesting.v10.expectList
import com.github.mvysny.ktormvaadin.AbstractDbTest
import com.github.mvysny.ktormvaadin.Person
import com.github.mvysny.ktormvaadin.Persons
import com.github.mvysny.ktormvaadin.db
import com.github.mvysny.ktormvaadin.ddl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.schema.ColumnDeclaring
import kotlin.test.expect

/**
 * Tests that the interval produces the correct WHERE clause, by running the clause
 * against a table of persons aged 0..9.
 */
class NumberIntervalTest : AbstractDbTest() {
    @BeforeEach fun prepareTestData() {
        Persons.ddl()
        db {
            repeat(10) { Person { name = "test $it"; age = it }.save() }
        }
    }

    @AfterEach fun tearDownTestData() {
        db { ddl("drop table if exists person") }
    }

    /**
     * @return ages of all persons matching given [condition], sorted ascending. A null
     * condition matches all persons.
     */
    private fun ages(condition: ColumnDeclaring<Boolean>?): List<Int> = db {
        var seq = database.sequenceOf(Persons)
        if (condition != null) {
            seq = seq.filter { condition }
        }
        seq.toList().map { it.age!! } .sorted()
    }

    @Test fun universalSetProducesNoCondition() {
        expect(null) { Persons.age.between(NumberInterval.ofInt(null, null)) }
        expectList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9) { ages(null) }
    }

    @Test fun singleItemMatchesExactly() {
        expectList(5) { ages(Persons.age.between(NumberInterval.ofInt(5, 5))) }
    }

    @Test fun lowerBoundOnly() {
        expectList(7, 8, 9) { ages(Persons.age.between(NumberInterval.ofInt(7, null))) }
    }

    @Test fun upperBoundOnly() {
        expectList(0, 1, 2) { ages(Persons.age.between(NumberInterval.ofInt(null, 2))) }
    }

    @Test fun bothBounds() {
        expectList(3, 4, 5) { ages(Persons.age.between(NumberInterval.ofInt(3, 5))) }
    }

    @Test fun emptyIntervalMatchesNothing() {
        expectList { ages(Persons.age.between(NumberInterval.ofInt(5, 3))) }
    }

    @Test fun asLongInterval() {
        expect(NumberInterval.ofLong(3L, 5L)) { NumberInterval.ofInt(3, 5).asLongInterval() }
        expect(NumberInterval.ofLong(null, null)) { NumberInterval.ofInt(null, null).asLongInterval() }
    }

    @Test fun asIntegerInterval() {
        expect(NumberInterval.ofInt(3, 5)) { NumberInterval.ofLong(3L, 5L).asIntegerInterval() }
        expect(NumberInterval.ofInt(null, null)) { NumberInterval.ofLong(null, null).asIntegerInterval() }
    }

    /**
     * [NumberRangePopup] produces Double intervals; make sure such an interval can be
     * converted and applied to an Int column, the way [NumberRangePopup] users do it.
     */
    @Test fun doubleIntervalAppliedToIntColumn() {
        expectList(3, 4, 5) { ages(Persons.age.between(NumberInterval(3.0, 5.0).asIntegerInterval())) }
    }
}
