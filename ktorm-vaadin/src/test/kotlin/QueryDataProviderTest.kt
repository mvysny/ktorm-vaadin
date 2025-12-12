package com.github.mvysny.ktormvaadin

import com.github.mvysny.kaributesting.v10.expectList
import com.github.mvysny.kaributesting.v10.expectRow
import com.github.mvysny.kaributesting.v10.expectRows
import com.github.mvysny.kaributools.fetchAll
import com.github.mvysny.kaributools.sort
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.provider.Query
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull
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
        Persons.ddl()
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
    @Test
    fun settableFilter() {
        e.setFilter(Persons.age eq 5)
        expect(1) { e.size(Query()) }
        expectList("test 6/5=street 5/city 5") { e.fetchAll().map { it.toString() } }
    }
    @Test
    fun queryFilter() {
        expect(1) { e.sizeFilter(Persons.age eq 5) }
        expectList("test 6/5=street 5/city 5") { e.fetchFilter(Persons.age eq 5).map { it.toString() } }
    }
    @Test
    fun queryBothFilters() {
        e.setFilter(Persons.age lte 5)
        expect(1) { e.sizeFilter(Persons.age gte 5) }
        expectList("test 6/5=street 5/city 5") { e.fetchFilter(Persons.age gte 5).map { it.toString() } }
    }
    @Test
    fun queryAll() {
        e.setFilter(Persons.age lte 7)
        expect(5) { e.sizeFilter(Persons.age gte 3) }
        expectList("test 5/4=street 4/city 4", "test 6/5=street 5/city 5") { e.fetchFilter(Persons.age gte 3, 1, 2).map { it.toString() } }
        expectList("test 7/6=street 6/city 6", "test 6/5=street 5/city 5") { e.fetchFilter(Persons.age gte 3, 1, 2, listOf(Persons.age.q.desc)).map { it.toString() } }
    }
    @Test
    fun stringFilter() {
        val dp = e.withStringFilterOn(Persons.name)
        expect(1) { dp.sizeFilter("test 5")}
        expectList("test 5/4=street 4/city 4") { dp.fetchFilter("test 5").map { it.toString() }}
        expect(2) { dp.sizeFilter("test 1")}
        expectList("test 1/0=street 0/city 0", "test 10/9=street 9/city 9") { dp.fetchFilter("test 1").map { it.toString() }}
        expect(10) { dp.sizeFilter("test ")}
    }
    @Test
    fun testWithGridSorting() {
       val g = Grid<PersonAddress>()
        g.dataProvider = e
        g.addColumn { it.person.name } .apply {
            setHeader("Name")
            key = Persons.name.q.key
            isSortable = true
        }
        g.sort(Persons.name.q.desc)
        g.expectRows(10)
        g.expectRow(0, "test 9")
    }
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

    @get:NotNull
    @get:NotBlank
    @get:Size(min = 1, max = 255)
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
            { it.from(Addresses).leftJoin(Persons, on = Addresses.of_person_id eq Persons.id) },
            { it.select(*Addresses.columns.toTypedArray(), *Persons.columns.toTypedArray())},
            { from(it) }
        )
    }
    override fun toString(): String = "${person.name}/${person.age}=${address.street}/${address.city}"
}