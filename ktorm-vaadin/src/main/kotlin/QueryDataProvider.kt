package com.github.mvysny.ktormvaadin

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.function.SerializableFunction
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.expression.OrderByExpression
import org.ktorm.schema.Column
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.Table
import org.ktorm.support.postgresql.ilike
import java.util.stream.Stream
import kotlin.collections.map

/**
 * Loads data from a ktorm [Query]. Mostly used for more complex stuff like joins; for selecting
 * entities use [EntityDataProvider]. Example of use:
 * ```
 * val dp = QueryDataProvider(
 *   { it.from(Reviews).leftJoin(Categories, on = Reviews.category eq Categories.id) },
 *   { it.select(*Reviews.columns.toTypedArray(), Categories.name)},
 *   { ReviewWithCategory.from(it) })
 * val filter = Reviews.name.ilike(normalizedFilter) or
 *   Categories.name.ilike(normalizedFilter)
 * dp.setFilter(filter)
 * ```
 * @param query creates [org.ktorm.dsl.Query]
 * @param rowMapper converts [QueryRowSet] to the bean [T]
 * @param T the bean type returned by this data provider.
 */
class QueryDataProvider<T>(val querySource: (Database) -> QuerySource, val query: (QuerySource) -> org.ktorm.dsl.Query, val rowMapper: (QueryRowSet) -> T) : AbstractBackEndDataProvider<T, ColumnDeclaring<Boolean>>(),
    ConfigurableFilterDataProvider<T, ColumnDeclaring<Boolean>, ColumnDeclaring<Boolean>> {

    private var filter: ColumnDeclaring<Boolean>? = null

    private fun calculateFilter(query: Query<T, ColumnDeclaring<Boolean>>): ColumnDeclaring<Boolean>? {
        val filter = this.filter
        val filter2 = query.filter.orElse(null)
        if (filter == null) return filter2
        if (filter2 == null) return filter
        return filter.and(filter2)
    }

    private val Query<T, ColumnDeclaring<Boolean>>.orderBy: List<OrderByExpression> get() {
        return sortOrders.map { sortOrder ->
            val table =querySource(ActiveKtorm.database).sourceTable
            // @TODO this only takes the first table into account!
            val column = try {
                table[sortOrder.sorted]
            } catch (e: NoSuchElementException) {
                throw RuntimeException("No column with name ${sortOrder.sorted}. Available column names: ${table.columns.map { it.name }}", e)
            }
            if (sortOrder.direction == SortDirection.ASCENDING) column.asc() else column.desc()
        }
    }

    override fun fetchFromBackEnd(query: Query<T, ColumnDeclaring<Boolean>>): Stream<T> = db {
        var q: org.ktorm.dsl.Query = query(querySource(database))
        val filter = calculateFilter(query)
        if (filter != null) {
            q = q.where(filter)
        }
        q = q.offset(query.offset).limit(query.limit).orderBy(query.orderBy)
        val result = q.map(rowMapper)
        result.stream()
    }

    override fun sizeInBackEnd(query: Query<T, ColumnDeclaring<Boolean>>): Int = db {
        var q: org.ktorm.dsl.Query = querySource(database).select(count())
        val filter = calculateFilter(query)
        if (filter != null) {
            q = q.where(filter)
        }
        val rowSet = q.rowSet

        if (rowSet.size() == 1) {
            check(rowSet.next())
            rowSet.getInt(1)
        } else {
            val (sql, _) = database.formatExpression(q.expression, beautifySql = true)
            throw IllegalStateException("Expected 1 row but ${rowSet.size()} returned from sql: \n\n$sql")
        }
    }

    override fun setFilter(filter: ColumnDeclaring<Boolean>?) {
        this.filter = filter
        refreshAll()
    }

    /**
     * Converts this data provider to one which accepts a [String] filter value. The string filter
     * is converted via [filterConverter] to a KTORM where clause. Perfect for using this data provider
     * with ComboBox (or other field which allows text-based search).
     * @param filterConverter converts String filter to a WHERE clause.
     * @return [DataProvider]
     */
    fun withStringFilter(filterConverter: SerializableFunction<String, ColumnDeclaring<Boolean>?>): DataProvider<T, String> {
        return withConvertedFilter { filter: String? ->
            val postProcessedFilter = filter?.trim() ?: ""
            if (postProcessedFilter.isNotEmpty()) filterConverter.apply(
                postProcessedFilter
            ) else null
        }
    }
}

/**
 * Returns a [DataProvider] which accepts a [String] filter; when a non-blank String is provided,
 * a `col ilike string%` where clause is added to the query.
 * Example of use:
 * ```
 * setItems(Categories.dataProvider.withStringFilterOn(Categories.name))
 * ```
 * @param column the [Table] column present in the select returning [T]
 * @param T the bean which holds the data provider values.
 * @return [DataProvider] which matches filter string against the value of given [column].
 */
fun <T> QueryDataProvider<T>.withStringFilterOn(column: Column<String>): DataProvider<T, String> =
    withStringFilter {
        column.ilike("${it.trim()}%")
    }
