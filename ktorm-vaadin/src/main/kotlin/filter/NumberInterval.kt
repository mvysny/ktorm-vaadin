package com.github.mvysny.ktormvaadin.filter
import com.github.mvysny.ktormvaadin.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.gte
import org.ktorm.dsl.lte
import org.ktorm.schema.Column
import org.ktorm.schema.ColumnDeclaring
import java.io.Serializable

/**
 * A potentially open numeric range. If both [start] and [endInclusive] are `null`, then the interval accepts any number.
 *
 * Immutable, thread-safe.
 * @param start The minimum number in the range.
 * @param endInclusive The maximum number still contained in the range (inclusive).
 * @param <T> the type of the numbers, contained in this interval. Usually [Long] or [Integer].
 */
class NumberInterval<T>(start: T?, endInclusive: T?) :
    ClosedInterval<T>(start, endInclusive),
    Serializable where T : Number, T : Comparable<T> {
    /**
     * Converts this interval to an [Long] interval. The returned interval contains the same values as the original one.
     * Used to e.g. convert Integer interval to Long.
     * @return the interval containing [Long] values.
     */
    fun asLongInterval(): NumberInterval<Long> = NumberInterval(start?.toLong(), endInclusive?.toLong())

    /**
     * Converts this interval to an [Integer] interval. The returned interval contains the same values as the original one.
     * Used to e.g. convert Long interval to Integer.
     * @return the interval containing [Integer] values.
     */
    fun asIntegerInterval(): NumberInterval<Int> =
        NumberInterval(start?.toInt(), endInclusive?.toInt())

    fun contains(expression: Column<T>): ColumnDeclaring<Boolean>? {
        if (isSingleItem) {
            return expression eq endInclusive!!
        }
        if (isUniversalSet) return null
        val e1 = if (start == null) null else expression gte start
        val e2 = if (endInclusive == null) null else expression lte endInclusive
        return listOf(e1, e2).and()
    }

    companion object {
        /**
         * Creates a numeric range, accepting values from given interval.
         * @param start the start of the interval; the smallest number that's still included in the interval.
         * @param endInclusive the end of the interval; the largest number that's still included in the interval.
         * @return the interval
         */
        fun ofLong(start: Long?, endInclusive: Long?): NumberInterval<Long> =
            NumberInterval(start, endInclusive)

        /**
         * Creates a numeric range, accepting values from given interval.
         * @param start the start of the interval; the smallest number that's still included in the interval.
         * @param endInclusive the end of the interval; the largest number that's still included in the interval.
         * @return the interval
         */
        fun ofInt(start: Int?, endInclusive: Int?): NumberInterval<Int> =
            NumberInterval(start, endInclusive)
    }
}

fun <T> Column<T>.between(interval: NumberInterval<T>) where T : Number, T : Comparable<T> =
    interval.contains(this)