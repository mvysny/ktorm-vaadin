package testapp

import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.kaributools.navigateTo
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.SortDirection
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect

class DepartmentsRouteTest : AbstractAppTest() {
    @BeforeEach
    fun navigate() {
        navigateTo<DepartmentsRoute>()
        _expectOne<DepartmentsRoute>()
    }

    @Test
    fun smoke() {
        _get<Grid<Department>>().expectRows(101)
        _get<Grid<Department>>().expectRowRegex(0, "\\d+", "Dept 0", "Somewhere 0")
    }

    @Test
    fun testNameFilter() {
        _get<TextField> { id = "name_filter" }._value = "Dept 1"
        _get<Grid<Department>>().expectRowRegex(0, "\\d+", "Dept 1", "Somewhere 1")
        _get<Grid<Department>>().expectRows(12)
    }

    @Test
    fun testLocationFilter() {
        _get<TextField> { id = "location_filter" }._value = "Somewhere 2"
        _get<Grid<Department>>().expectRowRegex(0, "\\d+", "Dept 2", "Somewhere 2")
        _get<Grid<Department>>().expectRows(11)
    }

    @Test
    fun testNameAndJobFilter() {
        _get<TextField> { id = "name_filter" }._value = "Dept 1"
        _get<TextField> { id = "location_filter" }._value = "Somewhere 2"
        _get<Grid<Department>>().expectRows(0)
    }

    @Test
    fun testSorting() {
        val grid = _get<Grid<Department>>()
        grid.expectRowRegex(0, "\\d+", "Dept 0", "Somewhere 0")
        grid.columns.filter { it.isSortable }.forEach {
            grid.sort(listOf(GridSortOrder(it, SortDirection.DESCENDING)))
            grid.expectRows(101)
            grid._findAll()
        }
    }

    @Nested
    inner class DepartmentFormTest {
        val form = DepartmentForm()
        @Test
        fun readValues() {
            form.binder.readBean(Department { name = "Foo"; location = "Bar" })
            expect("Foo") { form._get<TextField> { id = "name" }._value }
            expect("Bar") { form._get<TextField> { id = "location" }._value }
        }
        @Test
        fun writeValues() {
            form._get<TextField> { id = "name" }._value = "Foo"
            form._get<TextField> { id = "location" }._value = "Bar"
            val bean = Department{}
            form.binder.writeBean(bean)
            expect(Department{ name = "Foo"; location = "Bar" }) { bean }
        }
        @Test
        fun emptyFormWontValidate() {
           expect(false) { form.binder.writeBeanIfValid(Department{})}
        }
    }
}