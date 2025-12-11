package com.github.mvysny.ktormvaadin.filter

import com.github.mvysny.ktormvaadin.Persons
import org.junit.jupiter.api.Test
import kotlin.test.expect

class NumberIntervalTest {
    @Test fun toCondition() {
        expect(null) { Persons.id.between(NumberInterval.ofInt(null, null)) }
    }
}
