package com.github.mvysny.ktormvaadin

import com.github.mvysny.kaributesting.v10.expectList
import com.github.mvysny.kaributools.fetchAll
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.QuerySortOrder
import com.vaadin.flow.data.provider.SortDirection
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktorm.dsl.eq
import org.ktorm.dsl.gte
import org.ktorm.dsl.lte
import org.ktorm.schema.Column
import org.ktorm.schema.ColumnDeclaring
import kotlin.test.expect

class EntityDataProviderTest : AbstractDbTest() {
    private val e = EntityDataProvider(Persons)

    @BeforeEach
    fun prepareTestData() {
        db { ddl("create table person (id int not null primary key auto_increment, name varchar(255) not null, age int not null)") }
        db {
            repeat(10) {
                Person { name = "test ${it + 1}"; age = it }.save()
            }
        }
    }

    @AfterEach
    fun tearDownTestData() {
        db { ddl("drop table if exists person") }
    }

    @Test
    fun smoke() {
        expect(10) { e.fetchAll().size }
    }

    @Test
    fun sortingByName() {
        val p = e.fetchSortBy(Persons.name.asc)
        expect(10) { p.size }
        expectList("test 1", "test 10", "test 2", "test 3", "test 4", "test 5", "test 6", "test 7", "test 8", "test 9") { p.map { it.name } }
    }
    @Test
    fun sortingByNameDesc() {
        val p = e.fetchSortBy(Persons.name.desc)
        expect(10) { p.size }
        expectList("test 9", "test 8", "test 7", "test 6", "test 5", "test 4", "test 3", "test 2", "test 10", "test 1") { p.map { it.name } }
    }
    @Test
    fun sortingByAge() {
        val p = e.fetchSortBy(Persons.age.asc)
        expect(10) { p.size }
        expectList("test 1", "test 2", "test 3", "test 4", "test 5", "test 6", "test 7", "test 8", "test 9", "test 10") { p.map { it.name } }
    }
    @Test
    fun sortingByAgeDesc() {
        val p = e.fetchSortBy(Persons.age.desc)
        expect(10) { p.size }
        expectList("test 10", "test 9", "test 8", "test 7", "test 6", "test 5", "test 4", "test 3", "test 2", "test 1") { p.map { it.name } }
    }

    @Test
    fun settableFilter() {
        e.setFilter(Persons.age eq 5)
        expect(1) { e.size(Query()) }
        expectList("test 6") { e.fetchAll().map { it.name } }
    }
    @Test
    fun queryFilter() {
        expect(1) { e.sizeFilter(Persons.age eq 5) }
        expectList("test 6") { e.fetchFilter(Persons.age eq 5).map { it.name } }
    }
    @Test
    fun queryBothFilters() {
        e.setFilter(Persons.age lte 7)
        expect(5) { e.sizeFilter(Persons.age gte 3) }
        expectList("test 4", "test 5", "test 6", "test 7", "test 8") { e.fetchFilter(Persons.age gte 3).map { it.name } }
    }
}

private val Column<*>.asc: QuerySortOrder get() = QuerySortOrder(name, SortDirection.ASCENDING)
private val Column<*>.desc: QuerySortOrder get() = QuerySortOrder(name, SortDirection.DESCENDING)
private fun EntityDataProvider<Person>.fetchSortBy(vararg qs: QuerySortOrder): List<Person> = fetch(Query(
    0, Int.MAX_VALUE, qs.toList(), null, null
)).toList()
private fun EntityDataProvider<Person>.fetchFilter(f: ColumnDeclaring<Boolean>): List<Person> = fetch(Query(
    0, Int.MAX_VALUE, listOf(Persons.name.asc), null, f
)).toList()
private fun EntityDataProvider<Person>.sizeFilter(f: ColumnDeclaring<Boolean>): Int = size(Query(
    0, Int.MAX_VALUE, listOf(), null, f
))
