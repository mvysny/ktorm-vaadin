package com.github.mvysny.ktormvaadin.filter

import com.gitlab.mvysny.jdbiorm.condition.Condition
import com.gitlab.mvysny.jdbiorm.condition.Expression
import com.gitlab.mvysny.jdbiorm.vaadin.Person
import org.junit.jupiter.api.Test
import kotlin.test.expect

fun <N> Expression<N>.between2(i: NumberInterval<N>): Condition where N: Number, N: Comparable<N> = i.contains(this)

class NumberIntervalTest {
    @Test fun toCondition() {
        expect(Condition.NO_CONDITION) { Person.ID.between2(NumberInterval.ofLong(null, null)) }
        expect(true) { Expression.Value(3L).between2(NumberInterval.ofLong(3, null)).test("ignored") }
        expect(true) { Expression.Value(4L).between2(NumberInterval.ofLong(3, null)).test("ignored") }
        expect(false) { Expression.Value(2L).between2(NumberInterval.ofLong(3, null)).test("ignored") }
        expect(true) { Expression.Value(3L).between2(NumberInterval.ofLong(3, 6)).test("ignored") }
        expect(true) { Expression.Value(4L).between2(NumberInterval.ofLong(3, 6)).test("ignored") }
        expect(false) { Expression.Value(2L).between2(NumberInterval.ofLong(3, 6)).test("ignored") }
        expect(false) { Expression.Value(7L).between2(NumberInterval.ofLong(3, 6)).test("ignored") }
        expect(true) { Expression.Value(6L).between2(NumberInterval.ofLong(3, 6)).test("ignored") }
        expect(true) { Expression.Value(5L).between2(NumberInterval.ofLong(3, 6)).test("ignored") }
        expect(false) { Expression.Value(7L).between2(NumberInterval.ofLong(null, 6)).test("ignored") }
        expect(true) { Expression.Value(6L).between2(NumberInterval.ofLong(null, 6)).test("ignored") }
        expect(true) { Expression.Value(5L).between2(NumberInterval.ofLong(null, 6)).test("ignored") }
    }
}
