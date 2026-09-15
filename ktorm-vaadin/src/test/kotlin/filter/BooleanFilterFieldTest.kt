package com.github.mvysny.ktormvaadin.filter

import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10._value
import com.github.mvysny.kaributesting.v10.getSuggestionItems
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.expect

class BooleanFilterFieldTest {
    @BeforeEach fun setup() { MockVaadin.setup() }
    @AfterEach fun teardown() { MockVaadin.tearDown() }

    @Test fun smoke() {
        BooleanFilterField()
    }

    @Test fun offersTrueAndFalse() {
        expect(listOf(true, false)) { BooleanFilterField().getSuggestionItems() }
    }

    /**
     * null means "don't filter at all", and must therefore be reachable via the clear button.
     */
    @Test fun valueIsNullByDefaultAndClearable() {
        val f = BooleanFilterField()
        expect(null) { f._value }
        f._value = true
        expect(true) { f._value }
        f._value = null
        expect(null) { f._value }
    }
}
