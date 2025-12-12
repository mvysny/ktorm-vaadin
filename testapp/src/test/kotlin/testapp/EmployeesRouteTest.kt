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
import org.junit.jupiter.api.Test

class EmployeesRouteTest : AbstractAppTest() {
    @BeforeEach fun navigate() {
        navigateTo<EmployeesRoute>()
        _expectOne<EmployeesRoute>()
    }
    @Test
    fun smoke() {
        _get<Grid<Employee>>().expectRows(112)
    }

    @Test
    fun testNameFilter() {
        _get<TextField> { id = "name_filter" }._value = "Manager 1"
        _get<Grid<Employee>>().expectRowRegex(0, "\\d+", "Manager 1", "Manager", "2025-11-12", "6001", "Dept .*")
        _get<Grid<Employee>>().expectRows(2)
    }

    @Test
    fun testJobFilter() {
        _get<TextField> { id = "job_filter" }._value = "Manager"
        _get<Grid<Employee>>().expectRowRegex(0, "\\d+", "Manager 0", "Manager", "2025-11-12", "6000", "Dept .*")
        _get<Grid<Employee>>().expectRows(11)
    }

    @Test
    fun testNameAndJobFilter() {
        _get<TextField> { id = "name_filter" }._value = "Employee"
        _get<TextField> { id = "job_filter" }._value = "Manager"
        _get<Grid<Employee>>().expectRows(0)
    }

    @Test
    fun testDeptFilter() {
        _get<TextField> { id = "dept_filter" }._value = "n/a"
        _get<Grid<Employee>>().expectRows(0)
    }

    @Test fun testSortingSmoke() {
        val grid = _get<Grid<Employee>>()
        grid.expectRowRegex(0, "\\d+", "Employee 0", "Employee", "2025-11-12", "6000", "Dept .*")
        grid.columns.filter { it.isSortable } .forEach {
            grid.sort(listOf(GridSortOrder(it, SortDirection.DESCENDING)))
            grid.expectRows(112)
            grid._findAll()
        }
    }
}