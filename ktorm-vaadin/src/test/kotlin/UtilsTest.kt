package com.github.mvysny.ktormvaadin

import org.junit.jupiter.api.Test
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.gte
import org.ktorm.schema.ColumnDeclaring
import kotlin.test.expect

/**
 * Tests [and] - the function which joins filter components' conditions into one WHERE clause.
 */
class UtilsTest {
    private val ageIs5 = Persons.age eq 5
    private val ageIsAtLeast3 = Persons.age gte 3

    @Test fun emptyListProducesNoCondition() {
        expect(null) { listOf<ColumnDeclaring<Boolean>>().and() }
    }

    @Test fun listOfNullsProducesNoCondition() {
        expect(null) { listOf<ColumnDeclaring<Boolean>?>(null, null).and() }
    }

    @Test fun singleConditionIsPassedThrough() {
        expect(ageIs5) { listOf(ageIs5).and() }
    }

    @Test fun nullConditionsAreSkipped() {
        expect(ageIs5) { listOf(null, ageIs5, null).and() }
    }

    @Test fun multipleConditionsAreAnded() {
        expect(ageIs5.and(ageIsAtLeast3)) { listOf(ageIs5, ageIsAtLeast3).and() }
        expect(ageIs5.and(ageIsAtLeast3)) { listOf(null, ageIs5, null, ageIsAtLeast3).and() }
    }
}
