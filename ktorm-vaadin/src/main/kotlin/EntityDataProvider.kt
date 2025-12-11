package com.github.mvysny.ktormvaadin

import com.vaadin.flow.data.provider.*
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.function.SerializableFunction
import org.ktorm.dsl.*
import org.ktorm.entity.Entity
import org.ktorm.entity.count
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.expression.OrderByExpression
import org.ktorm.schema.*
import org.ktorm.support.postgresql.ilike
import java.util.stream.Stream

/**
 * Loads entities [T] from given [table]. Example of use:
 * ```
 * val dp = Employees.dataProvider
 * dp.setFilter(Employees.name.ilike(normalizedFilter))
 * ```
 * To enable sorting in a Grid, set the [Column.name] as a key to Vaadin column.
 * @param table the table to load the entities from
 * @param T entity type
 */
class EntityDataProvider<T: Entity<T>>(val table: Table<T>) : AbstractBackEndDataProvider<T, ColumnDeclaring<Boolean>>(),
    ConfigurableFilterDataProvider<T, ColumnDeclaring<Boolean>, ColumnDeclaring<Boolean>> {

    private var filter: ColumnDeclaring<Boolean>? = null

    private fun calculateFilter(query: Query<T, ColumnDeclaring<Boolean>>): ColumnDeclaring<Boolean>? {
        val filter = this.filter
        val filter2 = query.filter.orElse(null)
        if (filter == null) return filter2
        if (filter2 == null) return filter
        return filter.and(filter2)
    }

    private val Query<T, ColumnDeclaring<Boolean>>.orderBy: List<OrderByExpression>
        get() = sortOrders.map { sortOrder ->
            val column = try {
                table[sortOrder.sorted]
            } catch (e: NoSuchElementException) {
                throw RuntimeException("No column with name ${sortOrder.sorted}. Available column names: ${table.columns.map { it.name }}", e)
            }
            if (sortOrder.direction == SortDirection.ASCENDING) column.asc() else column.desc()
        }

    override fun fetchFromBackEnd(query: Query<T, ColumnDeclaring<Boolean>>): Stream<T> = db {
        var q = database.from(table).select()
        val filter = calculateFilter(query)
        if (filter != null) {
            q = q.where(filter)
        }
        q = q.offset(query.offset).limit(query.limit).orderBy(query.orderBy)
        val result = q.map { table.createEntity(it) }
        result.stream()
    }

    override fun sizeInBackEnd(query: Query<T, ColumnDeclaring<Boolean>>): Int = db {
        var seq = database.sequenceOf(table)
        val filter = calculateFilter(query)
        if (filter != null) {
            seq = seq.filter { filter }
        }
        seq.count()
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
 * Returns Vaadin {@link EntityDataProvider} which loads instances of this entity. See {@link EntityDataProvider}
 * for more information.
 */
val <E: Entity<E>> Table<E>.dataProvider: EntityDataProvider<E> get() = EntityDataProvider(this)

/**
 * Returns a [DataProvider] which accepts a [String] filter; when a non-blank String is provided,
 * a `col ilike string%` where clause is added to the query.
 * Example of use:
 * ```
 * setItems(Categories.dataProvider.withStringFilterOn(Categories.name))
 * ```
 * @param column the [Table] column from entity [T]
 * @param T the entity type
 * @return [DataProvider] which matches filter string against the value of given [column].
 */
fun <T: Entity<T>> EntityDataProvider<T>.withStringFilterOn(column: Column<String>): DataProvider<T, String> =
    withStringFilter {
        column.ilike("${it.trim()}%")
    }

/**
 * Provides column keys for [EntityDataProvider]-based grids.
 */
val Column<*>.e: EntityDataProviderColumnKey get() = EntityDataProviderColumnKey(this)

/**
 * Provides column keys for [EntityDataProvider]-based grids.
 */
data class EntityDataProviderColumnKey(val column: Column<*>) {
    /**
     * @return key Use this value for Vaadin Grid Column key.
     */
    val key: String get() = column.name
    val asc: QuerySortOrder get() = QuerySortOrder(key, SortDirection.ASCENDING)
    val desc: QuerySortOrder
        get() = QuerySortOrder(
            key,
            SortDirection.DESCENDING
        )
}