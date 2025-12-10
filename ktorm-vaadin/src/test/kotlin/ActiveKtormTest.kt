package com.github.mvysny.ktormvaadin

import org.junit.jupiter.api.Test
import kotlin.test.expect

class ActiveKtormTest {
    @Test fun smoke() {
        ActiveKtorm.validator
        setupDatabase()
        try {
            expect("jdbc:h2:mem:test") { ActiveKtorm.database.url }
        } finally {
            tearDownDatabase()
        }
    }
}