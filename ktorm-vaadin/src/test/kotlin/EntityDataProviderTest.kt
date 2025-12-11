package com.github.mvysny.ktormvaadin

import com.github.mvysny.kaributesting.v10.expectList
import com.github.mvysny.kaributools.fetchAll
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.QuerySortOrder
import com.vaadin.flow.data.provider.SortDirection
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
        val q = Query<Person, ColumnDeclaring<Boolean>>(
            0,
            Int.MAX_VALUE,
            listOf(QuerySortOrder(Persons.name.name, SortDirection.ASCENDING)),
            null,
            null
        )
        val p = e.fetch(q).toList()
        expect(10) { p.size }
        expectList("test 1", "test 10", "test 2", "test 3", "test 4", "test 5", "test 6", "test 7", "test 8", "test 9") { p.map { it.name } }
    }
    @Test
    fun sortingByNameDesc() {
        val q = Query<Person, ColumnDeclaring<Boolean>>(
            0,
            Int.MAX_VALUE,
            listOf(QuerySortOrder(Persons.name.name, SortDirection.DESCENDING)),
            null,
            null
        )
        val p = e.fetch(q).toList()
        expect(10) { p.size }
        expectList("test 9", "test 8", "test 7", "test 6", "test 5", "test 4", "test 3", "test 2", "test 10", "test 1") { p.map { it.name } }
    }
}
