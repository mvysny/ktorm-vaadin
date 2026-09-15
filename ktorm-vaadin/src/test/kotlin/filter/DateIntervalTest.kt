package com.github.mvysny.ktormvaadin.filter

import com.github.mvysny.kaributesting.v10.expectList
import com.github.mvysny.ktormvaadin.AbstractDbTest
import com.github.mvysny.ktormvaadin.ActiveEntity
import com.github.mvysny.ktormvaadin.db
import com.github.mvysny.ktormvaadin.ddl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.ktorm.entity.Entity
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.int
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

    /**
     * Tests that the interval produces the correct WHERE clause, by running the clause
     * against a table of events happening on 2024-01-01..2024-01-10.
     */
    @Nested inner class ToSqlTests : AbstractDbTest() {
        @BeforeEach fun prepareTestData() {
            Events.ddl()
            db {
                repeat(10) { Event { day = day(it + 1) } .save() }
            }
        }

        @AfterEach fun tearDownTestData() {
            db { ddl("drop table if exists event") }
        }

        private fun day(dayOfMonth: Int) = LocalDate.of(2024, 1, dayOfMonth)

        /**
         * @return days of all events matching given [condition], sorted ascending. A null
         * condition matches all events.
         */
        private fun days(condition: ColumnDeclaring<Boolean>?): List<LocalDate> = db {
            var seq = database.sequenceOf(Events)
            if (condition != null) {
                seq = seq.filter { condition }
            }
            seq.toList().map { it.day!! } .sorted()
        }

        @Test fun universalSetProducesNoCondition() {
            expect(null) { Events.day.between(DateInterval.UNIVERSAL) }
            expect(10) { days(null).size }
        }

        @Test fun singleItemMatchesExactly() {
            expectList(day(5)) { days(Events.day.between(DateInterval.of(day(5)))) }
        }

        @Test fun lowerBoundOnly() {
            expectList(day(9), day(10)) { days(Events.day.between(DateInterval(day(9), null))) }
        }

        @Test fun upperBoundOnly() {
            expectList(day(1), day(2)) { days(Events.day.between(DateInterval(null, day(2)))) }
        }

        @Test fun bothBounds() {
            expectList(day(3), day(4), day(5)) { days(Events.day.between(DateInterval(day(3), day(5)))) }
        }

        @Test fun emptyIntervalMatchesNothing() {
            expectList { days(Events.day.between(DateInterval.EMPTY)) }
        }
    }
}

object Events : Table<Event>("event") {
    val id = int("id").primaryKey().bindTo { it.id }
    val day = date("event_day").bindTo { it.day }
    fun ddl() {
        db { ddl("create table event (id int not null primary key auto_increment, event_day date not null)") }
    }
}

interface Event : ActiveEntity<Event> {
    val id: Int?
    var day: LocalDate?

    override val table: Table<Event> get() = Events
    companion object : Entity.Factory<Event>()
}
