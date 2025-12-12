package com.github.mvysny.ktormvaadin

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.QuerySortOrder
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.function.SerializableFunction
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.expression.BinaryExpression
import org.ktorm.expression.ColumnDeclaringExpression
import org.ktorm.expression.OrderByExpression
import org.ktorm.expression.OrderType
import org.ktorm.expression.ScalarExpression
import org.ktorm.expression.SelectExpression
import org.ktorm.expression.SqlExpression
import org.ktorm.expression.UnaryExpression
import org.ktorm.expression.UnionExpression
import org.ktorm.schema.BaseTable
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
 *   { it.from(Reviews).leftJoin(Categories, on = Reviews.category eq Categories.id)
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
class QueryDataProvider<T>(
    val querySource: (Database) -> QuerySource,
    val query: (QuerySource) -> org.ktorm.dsl.Query,
    val rowMapper: (QueryRowSet) -> T
) : AbstractBackEndDataProvider<T, ColumnDeclaring<Boolean>>(),
    ConfigurableFilterDataProvider<T, ColumnDeclaring<Boolean>, ColumnDeclaring<Boolean>> {

    private var filter: ColumnDeclaring<Boolean>? = null

    private fun calculateFilter(query: Query<T, ColumnDeclaring<Boolean>>): ColumnDeclaring<Boolean>? {
        val filter = this.filter
        val filter2 = query.filter.orElse(null)
        if (filter == null) return filter2
        if (filter2 == null) return filter
        return filter.and(filter2)
    }

    /**
     * Walks through the tree of expressions and finds the one whose [Any.toString]
     * is identical to [toString]. In other words, we're trying to reconstruct a Ktorm
     * expression from a string representation.
     * @param expr the expression tree to examine
     * @param toString we're trying to find this expression
     * @return expression found, or null if nothing matched.
     */
    private fun findExpression(expr: SqlExpression?, toString: String): SqlExpression? {
        if (expr == null) return null
        if (expr.toString() == toString) {
            return expr
        }
        if (expr is SelectExpression) {
            expr.columns.forEach { expr ->
                findExpression(expr, toString).takeIf { it != null }?.let { return it }
            }
            findExpression(expr.from, toString).takeIf { it != null }?.let { return it }
            findExpression(expr.where, toString).takeIf { it != null }?.let { return it }
        }
        if (expr is ColumnDeclaringExpression<*>) {
            findExpression(expr.expression, toString).takeIf { it != null }?.let { return it }
        }
        if (expr is UnaryExpression<*>) {
            findExpression(expr.operand, toString).takeIf { it != null }?.let { return it }
        }
        if (expr is BinaryExpression<*>) {
            findExpression(expr.left, toString).takeIf { it != null }?.let { return it }
            findExpression(expr.right, toString).takeIf { it != null }?.let { return it }
        }
        if (expr is UnionExpression) {
            findExpression(expr.left, toString).takeIf { it != null }?.let { return it }
            findExpression(expr.right, toString).takeIf { it != null }?.let { return it }
        }
        return null
    }
    private fun findExpression(table: BaseTable<*>, toString: String): SqlExpression? =
        table.columns.map { it.asExpression() } .firstOrNull { it.toString() == toString }

    private val Query<T, ColumnDeclaring<Boolean>>.orderBy: List<OrderByExpression> get() {
        val selectExpr = query(querySource(ActiveKtorm.database)).expression
        return sortOrders.map { sortOrder ->
            val expr = findExpression(selectExpr, sortOrder.sorted)
            checkNotNull(expr) {
                "Expression ${sortOrder.sorted} not found in $selectExpr"
            }
            val ot = if (sortOrder.direction == SortDirection.ASCENDING) OrderType.ASCENDING else OrderType.DESCENDING
            OrderByExpression(expr as ScalarExpression<*>, ot)
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

/**
 * Provides column keys for [QueryDataProvider]-based grids.
 */
val Column<*>.q: QueryDataProviderColumnKey get() = QueryDataProviderColumnKey(this)

/**
 * Provides column keys for [QueryDataProvider]-based grids.
 */
data class QueryDataProviderColumnKey(val column: Column<*>) {
    /**
     * @return key Use this value for Vaadin Grid Column key.
     */
    val key: String get() = column.asExpression().toString()
    val asc: QuerySortOrder get() = QuerySortOrder(key, SortDirection.ASCENDING)
    val desc: QuerySortOrder get() = QuerySortOrder(key, SortDirection.DESCENDING)
}
