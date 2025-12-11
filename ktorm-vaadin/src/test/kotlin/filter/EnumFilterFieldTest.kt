package com.github.mvysny.ktormvaadin.filter

import org.junit.jupiter.api.Test
import kotlin.test.expect

enum class MaritalStatus {
    married,
    divorced,
    single
}

class EnumFilterFieldTest {
    @Test
    fun smoke() {
        val f = EnumFilterField(MaritalStatus::class.java)
        expect(true) { f.isAllOrNothingSelected }
    }
}
