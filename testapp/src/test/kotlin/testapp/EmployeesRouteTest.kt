package testapp

import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.kaributools.navigateTo
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.textfield.TextField
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EmployeesRouteTest : AbstractAppTest() {
    @Test
    fun smoke() {
        navigateTo<EmployeesRoute>()
        _expectOne<EmployeesRoute>()
        _get<Grid<Employee>>().expectRows(112)
    }

    @Test
    fun testNameFilter() {
        navigateTo<EmployeesRoute>()
        _get<TextField> { id = "name_filter" }._value = "Manager 1"
        _get<Grid<Employee>>().expectRowRegex(0, "\\d+", "Manager 1", "Manager", "2025-11-12", "6001")
        _get<Grid<Employee>>().expectRows(2)
    }

    @Test
    fun testJobFilter() {
        navigateTo<EmployeesRoute>()
        _get<TextField> { id = "job_filter" }._value = "Manager"
        _get<Grid<Employee>>().expectRowRegex(0, "\\d+", "Manager 0", "Manager", "2025-11-12", "6000")
        _get<Grid<Employee>>().expectRows(11)
    }

    @Test
    fun testNameAndJobFilter() {
        navigateTo<EmployeesRoute>()
        _get<TextField> { id = "name_filter" }._value = "Employee"
        _get<TextField> { id = "job_filter" }._value = "Manager"
        _get<Grid<Employee>>().expectRows(0)
    }

    @Test fun testSorting() {
        _get<Grid<Employee>>().expectRowRegex(0, "\\d+", "Employee 0", "Employee", "2025-11-12", "6000")
    }
}