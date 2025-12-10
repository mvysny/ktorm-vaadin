package com.github.mvysny.ktormvaadin

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

class ActiveKtormTest {
    @Test fun smoke() {
        ActiveKtorm.validator
    }

    @Nested inner class WithDB : AbstractDbTest() {
        @Test fun smoke() {
            expect("jdbc:h2:mem:test") { ActiveKtorm.database.url }
        }
    }
}