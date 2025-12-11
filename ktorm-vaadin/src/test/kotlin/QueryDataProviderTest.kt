package com.github.mvysny.ktormvaadin

import com.github.mvysny.kaributesting.v10.expectList
import com.github.mvysny.kaributesting.v10.expectRow
import com.github.mvysny.kaributesting.v10.expectRows
import com.github.mvysny.kaributools.fetchAll
import com.github.mvysny.kaributools.sort
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.provider.Query
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktorm.dsl.QueryRowSet
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.gte
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.lte
import org.ktorm.dsl.select
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import kotlin.test.expect

class QueryDataProviderTest : AbstractDbTest() {
    private val e = PersonAddress.dataProvider
    @BeforeEach
    fun prepareTestData() {
        Persons.create()
        Addresses.ddl()
        db {
            repeat(10) {
                val p = Person { name = "test ${it + 1}"; age = it }.save()
                Address { street = "street $it"; city = "city $it"; of_person_id = p.id } .save()
            }
        }
    }

    @AfterEach
    fun tearDownTestData() {
        db { ddl("drop table if exists person; drop table if exists addresses") }
    }

    @Test
    fun smoke() {
        expect(10) { e.fetchAll().size }
        expectList(
            "test 1/0=street 0/city 0",
            "test 2/1=street 1/city 1",
        ) { e.fetchAll().subList(0, 2).map { it.toString() } }
    }

    @Test
    fun sortingByStreet() {
        val p = e.fetchSortBy(Addresses.street.q.desc)
        expect(10) { p.size }
        expectList("test 10/9=street 9/city 9", "test 9/8=street 8/city 8") { p.take(2).map { it.toString() } }
    }

    @Test
    fun sortingByName() {
        val p = e.fetchSortBy(Persons.name.q.desc)
        expect(10) { p.size }
        expectList("test 9/8=street 8/city 8", "test 8/7=street 7/city 7") { p.take(2).map { it.toString() } }
    }
//    @Test
//    fun sortingByNameDesc() {
//        val p = e.fetchSortBy(Persons.name.e.desc)
//        expect(10) { p.size }
//        expectList("test 9", "test 8", "test 7", "test 6", "test 5", "test 4", "test 3", "test 2", "test 10", "test 1") { p.map { it.name } }
//    }
//    @Test
//    fun sortingByAge() {
//        val p = e.fetchSortBy(Persons.age.e.asc)
//        expect(10) { p.size }
//        expectList("test 1", "test 2", "test 3", "test 4", "test 5", "test 6", "test 7", "test 8", "test 9", "test 10") { p.map { it.name } }
//    }
//    @Test
//    fun sortingByAgeDesc() {
//        val p = e.fetchSortBy(Persons.age.e.desc)
//        expect(10) { p.size }
//        expectList("test 10", "test 9", "test 8", "test 7", "test 6", "test 5", "test 4", "test 3", "test 2", "test 1") { p.map { it.name } }
//    }
//
//    @Test
//    fun settableFilter() {
//        e.setFilter(Persons.age eq 5)
//        expect(1) { e.size(Query()) }
//        expectList("test 6") { e.fetchAll().map { it.name } }
//    }
//    @Test
//    fun queryFilter() {
//        expect(1) { e.sizeFilter(Persons.age eq 5) }
//        expectList("test 6") { e.fetchFilter(Persons.age eq 5).map { it.name } }
//    }
//    @Test
//    fun queryBothFilters() {
//        e.setFilter(Persons.age lte 7)
//        expect(5) { e.sizeFilter(Persons.age gte 3) }
//        expectList("test 4", "test 5", "test 6", "test 7", "test 8") { e.fetchFilter(Persons.age gte 3).map { it.name } }
//    }
//    @Test
//    fun queryAll() {
//        e.setFilter(Persons.age lte 7)
//        expect(5) { e.sizeFilter(Persons.age gte 3) }
//        expectList("test 5", "test 6") { e.fetchFilter(Persons.age gte 3, 1, 2).map { it.name } }
//        expectList("test 7", "test 6") { e.fetchFilter(Persons.age gte 3, 1, 2, listOf(Persons.age.e.desc)).map { it.name } }
//    }
//    @Test
//    fun stringFilter() {
//        val dp = e.withStringFilterOn(Persons.name)
//        expect(1) { dp.sizeFilter("test 5")}
//        expectList("test 5") { dp.fetchFilter("test 5").map { it.name }}
//        expect(2) { dp.sizeFilter("test 1")}
//        expectList("test 1", "test 10") { dp.fetchFilter("test 1").map { it.name }}
//        expect(10) { dp.sizeFilter("test ")}
//    }
//    @Test
//    fun testWithGridSorting() {
//       val g = Grid<Person>()
//        g.dataProvider = e
//        g.addColumn { it.name } .apply {
//            setHeader("Name")
//            key = Persons.name.e.key
//            isSortable = true
//        }
//        g.sort(Persons.name.e.desc)
//        g.expectRows(10)
//        g.expectRow(0, "test 9")
//    }
}

object Addresses : Table<Address>("addresses") {
    val id = int("id").primaryKey().bindTo { it.id }
    val street = varchar("street").bindTo { it.street }
    val city = varchar("city").bindTo { it.city }
    val of_person_id = int("of_person_id").bindTo { it.of_person_id }
    fun ddl() = db {
        ddl("create table addresses (id int primary key auto_increment, street varchar(255) not null, city varchar(255), of_person_id int)")
    }
}

interface Address : ActiveEntity<Address> {
    val id: Int?
    var street: String?
    var city: String?
    var of_person_id: Int?
    override val table: Table<Address>
        get() = Addresses

    companion object : Entity.Factory<Address>()
}

data class PersonAddress(val person: Person, val address: Address) {
    companion object {
        fun from(row: QueryRowSet): PersonAddress = PersonAddress(
            Persons.createEntity(row), Addresses.createEntity(row)
        )
        val dataProvider: QueryDataProvider<PersonAddress> get() = QueryDataProvider(
            listOf(Addresses, Persons),
            { it.from(Addresses).leftJoin(Persons, on = Addresses.of_person_id eq Persons.id) },
            { it.select(*Addresses.columns.toTypedArray(), *Persons.columns.toTypedArray())},
            { from(it) }
        )
    }
    override fun toString(): String = "${person.name}/${person.age}=${address.street}/${address.city}"
}