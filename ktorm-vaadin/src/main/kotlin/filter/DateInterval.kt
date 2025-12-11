package com.github.mvysny.ktormvaadin.filter
import com.github.mvysny.ktormvaadin.and
import org.ktorm.dsl.gte
import org.ktorm.dsl.lte
import org.ktorm.schema.Column
import org.ktorm.schema.ColumnDeclaring
import java.io.Serializable
import java.time.LocalDate
import java.time.ZoneId

/**
 * A potentially unbounded date range. If both [start] and [endInclusive] are `null`, then the interval accepts any date.
 *
 * Immutable, thread-safe.
 * @param start The minimum date in the range.
 * @param endInclusive The maximum date still contained in the range (inclusive).
 */
class DateInterval(start: LocalDate?, endInclusive: LocalDate?) : ClosedInterval<LocalDate>(start, endInclusive), Serializable {
    fun contains(expression: Column<LocalDate>): ColumnDeclaring<Boolean>? {
        val e1 = if (start == null) null else expression gte start
        val e2 = if (endInclusive == null) null else expression lte endInclusive
        return listOf(e1, e2).and()
    }

    companion object {
        /**
         * An empty interval which contains no items.
         */
        val EMPTY: DateInterval =
            DateInterval(LocalDate.of(2000, 1, 2), LocalDate.of(2000, 1, 1))

        /**
         * A universal interval which contains all possible dates.
         */
        val UNIVERSAL: DateInterval = DateInterval(null, null)

        /**
         * Produces a degenerate date interval that only contains [LocalDate.now].
         * @param zoneId the zone ID to use, not null.
         * @return an interval with just one item.
         */
        fun now(zoneId: ZoneId): DateInterval {
            return of(LocalDate.now(zoneId))
        }

        /**
         * Produces a degenerate date interval that only contains given date.
         * @param localDate the date
         * @return an interval with just one item.
         */
        fun of(localDate: LocalDate): DateInterval {
            return DateInterval(localDate, localDate)
        }
    }
}

fun Column<LocalDate>.between(interval: DateInterval): ColumnDeclaring<Boolean>? = interval.contains(this)