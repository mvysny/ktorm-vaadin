package com.github.mvysny.ktormvaadin.filter

import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.expect

class DateIntervalTest {
    @Test fun empty() {
        expect(true) { DateInterval.EMPTY.isEmpty }
        expect(false) { DateInterval.UNIVERSAL.isEmpty }
        expect(false) { DateInterval.of(LocalDate.of(2000, 1, 1)).isEmpty }
    }

    @Test fun isBound() {
        expect(true) { DateInterval.EMPTY.isBound }
        expect(false) { DateInterval.UNIVERSAL.isBound }
        expect(true) { DateInterval.of(LocalDate.of(2000, 1, 1)).isBound }
    }

    @Test fun isSingleItem() {
        expect(false) { DateInterval.EMPTY.isSingleItem }
        expect(false) { DateInterval.UNIVERSAL.isSingleItem }
        expect(true) { DateInterval.of(LocalDate.of(2000, 1, 1)).isSingleItem }
    }

    @Test fun isUniversalSet() {
        expect(false) { DateInterval.EMPTY.isUniversalSet }
        expect(true) { DateInterval.UNIVERSAL.isUniversalSet }
        expect(false) { DateInterval.of(LocalDate.of(2000, 1, 1)).isUniversalSet }
    }

    @Test fun contains() {
        val probe = LocalDate.of(2000, 1, 1)
        expect(false) { DateInterval.EMPTY.contains(probe) }
        expect(false) { DateInterval.EMPTY.contains(DateInterval.EMPTY.endInclusive!!) }
        expect(true) { DateInterval.UNIVERSAL.contains(probe) }
        expect(true) { DateInterval.of(probe).contains(probe) }
        expect(true) { DateInterval(LocalDate.of(1999, 1, 1), null).contains(probe) }
        expect(true) { DateInterval(null, LocalDate.of(2001, 1, 1)).contains(probe) }
        expect(false) { DateInterval(LocalDate.of(2000, 1, 2), null).contains(probe) }
        expect(false) { DateInterval(null, LocalDate.of(2000, 1, 1).minusDays(1)).contains(probe) }
    }
}
