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

class DepartmentsRouteTest : AbstractAppTest() {
    @Test
    fun smoke() {
        navigateTo<DepartmentsRoute>()
        _expectOne<DepartmentsRoute>()
        _get<Grid<Department>>().expectRows(112)
    }

    @Test
    fun testNameFilter() {
        navigateTo<DepartmentsRoute>()
        _get<TextField> { id = "name_filter" }._value = "Manager 1"
        _get<Grid<Department>>().expectRow(0, "2", "Manager 1", "Manager", "2025-11-12", "6001")
        _get<Grid<Department>>().expectRows(2)
    }

    @Test
    fun testJobFilter() {
        navigateTo<DepartmentsRoute>()
        _get<TextField> { id = "job_filter" }._value = "Manager"
        _get<Grid<Department>>().expectRow(0, "1", "Manager 0", "Manager", "2025-11-12", "6000")
        _get<Grid<Department>>().expectRows(11)
    }

    @Test
    fun testNameAndJobFilter() {
        navigateTo<DepartmentsRoute>()
        _get<TextField> { id = "name_filter" }._value = "Department"
        _get<TextField> { id = "job_filter" }._value = "Manager"
        _get<Grid<Department>>().expectRows(0)
    }

    @Test fun testSorting() {
        _get<Grid<Department>>().expectRow(0, "12", "Employee 0", "Employee", "2025-11-12", "6000")
    }
}