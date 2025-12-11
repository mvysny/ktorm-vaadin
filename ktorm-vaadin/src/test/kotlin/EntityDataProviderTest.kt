package com.github.mvysny.ktormvaadin

import com.github.mvysny.kaributesting.v10.expectList
import com.github.mvysny.kaributools.fetchAll
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.QuerySortOrder
import com.vaadin.flow.data.provider.SortDirection
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
}

private val Column<*>.asc: QuerySortOrder get() = QuerySortOrder(name, SortDirection.ASCENDING)
private val Column<*>.desc: QuerySortOrder get() = QuerySortOrder(name, SortDirection.DESCENDING)
private fun EntityDataProvider<Person>.fetchSortBy(vararg qs: QuerySortOrder): List<Person> = fetch(Query(
    0, Int.MAX_VALUE, qs.toList(), null, null
)).toList()