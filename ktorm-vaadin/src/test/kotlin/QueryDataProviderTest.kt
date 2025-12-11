package com.github.mvysny.ktormvaadin

import com.github.mvysny.kaributesting.v10.expectList
import com.github.mvysny.kaributesting.v10.expectRow
import com.github.mvysny.kaributesting.v10.expectRows
import com.github.mvysny.kaributools.fetchAll
import com.github.mvysny.kaributools.sort
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.QuerySortOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktorm.dsl.eq
import org.ktorm.dsl.gte
import org.ktorm.dsl.lte
import kotlin.test.expect

class QueryDataProviderTest : AbstractDbTest() {
    private val e = EntityDataProvider(Persons)

    @BeforeEach
    fun prepareTestData() {
        Persons.create()
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
        val p = e.fetchSortBy(Persons.name.e.asc)
        expect(10) { p.size }
        expectList("test 1", "test 10", "test 2", "test 3", "test 4", "test 5", "test 6", "test 7", "test 8", "test 9") { p.map { it.name } }
    }
    @Test
    fun sortingByNameDesc() {
        val p = e.fetchSortBy(Persons.name.e.desc)
        expect(10) { p.size }
        expectList("test 9", "test 8", "test 7", "test 6", "test 5", "test 4", "test 3", "test 2", "test 10", "test 1") { p.map { it.name } }
    }
    @Test
    fun sortingByAge() {
        val p = e.fetchSortBy(Persons.age.e.asc)
        expect(10) { p.size }
        expectList("test 1", "test 2", "test 3", "test 4", "test 5", "test 6", "test 7", "test 8", "test 9", "test 10") { p.map { it.name } }
    }
    @Test
    fun sortingByAgeDesc() {
        val p = e.fetchSortBy(Persons.age.e.desc)
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
    @Test
    fun queryAll() {
        e.setFilter(Persons.age lte 7)
        expect(5) { e.sizeFilter(Persons.age gte 3) }
        expectList("test 5", "test 6") { e.fetchFilter(Persons.age gte 3, 1, 2).map { it.name } }
        expectList("test 7", "test 6") { e.fetchFilter(Persons.age gte 3, 1, 2, listOf(Persons.age.e.desc)).map { it.name } }
    }
    @Test
    fun stringFilter() {
        val dp = e.withStringFilterOn(Persons.name)
        expect(1) { dp.sizeFilter("test 5")}
        expectList("test 5") { dp.fetchFilter("test 5").map { it.name }}
        expect(2) { dp.sizeFilter("test 1")}
        expectList("test 1", "test 10") { dp.fetchFilter("test 1").map { it.name }}
        expect(10) { dp.sizeFilter("test ")}
    }
    @Test
    fun testWithGridSorting() {
       val g = Grid<Person>()
        g.dataProvider = e
        g.addColumn { it.name } .apply {
            setHeader("Name")
            key = Persons.name.e.key
            isSortable = true
        }
        g.sort(Persons.name.e.desc)
        g.expectRows(10)
        g.expectRow(0, "test 9")
    }
}

