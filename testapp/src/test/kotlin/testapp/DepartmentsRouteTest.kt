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
        _get<Grid<Department>>().expectRows(101)
        _get<Grid<Department>>().expectRowRegex(0, "\\d+", "Dept 0", "Somewhere 0")
    }

    @Test
    fun testNameFilter() {
        navigateTo<DepartmentsRoute>()
        _get<TextField> { id = "name_filter" }._value = "Dept 1"
        _get<Grid<Department>>().expectRowRegex(0, "\\d+", "Dept 1", "Somewhere 1")
        _get<Grid<Department>>().expectRows(12)
    }

    @Test
    fun testLocationFilter() {
        navigateTo<DepartmentsRoute>()
        _get<TextField> { id = "location_filter" }._value = "Somewhere 2"
        _get<Grid<Department>>().expectRowRegex(0, "\\d+", "Dept 2", "Somewhere 2")
        _get<Grid<Department>>().expectRows(11)
    }

    @Test
    fun testNameAndJobFilter() {
        navigateTo<DepartmentsRoute>()
        _get<TextField> { id = "name_filter" }._value = "Dept 1"
        _get<TextField> { id = "location_filter" }._value = "Somewhere 2"
        _get<Grid<Department>>().expectRows(0)
    }

    @Test fun testSorting() {
        _get<Grid<Department>>().expectRowRegex(0, "\\d+", "Employee 0", "Employee", "2025-11-12", "6000")
    }
}