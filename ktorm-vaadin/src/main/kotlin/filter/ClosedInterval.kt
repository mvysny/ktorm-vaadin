package com.github.mvysny.ktormvaadin.filter

import java.util.*

/**
 * Represents a (potentially unbounded) range of values (for example, numbers or characters).
 *
 * The [start] or [endInclusive] (or both) may be null, to tell that there is no lower or upper boundary.
 * That may not make the interval infinite since some types are limited in value (for example LocalDate
 * has a minimum and a maximum value).
 * @property start The minimum value in this range.
 * @property endInclusive The maximum value still contained in this range (inclusive).
 * @param <T> the type of the items contained in this interval.
 */
open class ClosedInterval<T : Comparable<T>>(val start: T?, val endInclusive: T?) {
    /**
     * If true then this range accepts any value. True when both `start` and `endInclusive` are null.
     * @return true if this interval contains all possible values; false if the interval is bound at least from one side.
     */
    val isUniversalSet: Boolean
        get() = this.start == null && this.endInclusive == null

    /**
     * Checks whether the specified `value` belongs to the range.
     * @param value the value to check.
     * @return true if given value is contained in this interval, false if not.
     */
    operator fun contains(value: T): Boolean {
        val start = this.start
        val endInclusive = this.endInclusive
        val matchesLowerBoundary = start == null || start <= value
        if (!matchesLowerBoundary) {
            return false
        }
        return endInclusive == null || value <= endInclusive
    }

    /**
     * Checks whether the range is empty.
     * @return true if this interval contains no items; false if it contains 1 or more items.
     */
    val isEmpty: Boolean
        get() {
            val start = this.start ?: return false
            val end = this.endInclusive ?: return false
            return start > end
        }

    /**
     * True if the interval consists of single number only (is degenerate).
     * @return true if the interval contains exactly one item; false if the interval contains no items or if it contains 2 or more items.
     */
    val isSingleItem: Boolean
        get() = this.isBound && this.start!!.compareTo(endInclusive!!) == 0

    /**
     * True if the interval is bound (both [.getStart] and [.getEndInclusive] are not null).
     * @return false if the interval is open (contains infinite amount of numbers).
     */
    val isBound: Boolean
        get() = this.start != null && this.endInclusive != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClosedInterval<*>) return false
        return start == other.start && endInclusive == other.endInclusive
    }

    override fun hashCode(): Int = Objects.hash(start, endInclusive)

    override fun toString(): String = "$start..$endInclusive"
}