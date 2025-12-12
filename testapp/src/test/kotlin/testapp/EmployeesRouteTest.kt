package testapp

import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.kaributools.navigateTo
import com.github.mvysny.ktormvaadin.findAll
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSortOrder
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.SortDirection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.expect

class EmployeesRouteTest : AbstractAppTest() {
    @BeforeEach
    fun navigate() {
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

    @Test
    fun testSortingSmoke() {
        val grid = _get<Grid<Employee>>()
        grid.expectRowRegex(0, "\\d+", "Employee 0", "Employee", "2025-11-12", "6000", "Dept .*")
        grid.columns.filter { it.isSortable }.forEach {
            grid.sort(listOf(GridSortOrder(it, SortDirection.DESCENDING)))
            grid.expectRows(112)
            grid._findAll()
        }
    }

    @Nested
    inner class EmployeeFormTest {
        val form = EmployeeForm()
        val employee = Employees.findAll()[0]

        @Test
        fun readValues() {
            form.binder.readBean(employee)
            expect("Manager 0") { form._get<TextField> { id = "name" }._value }
            expect("Manager") { form._get<TextField> { id = "job" }._value }
        }

        @Test
        fun writeValues() {
            form._get<TextField> { id = "name" }._value = "Foo"
            form._get<TextField> { id = "job" }._value = "Bar"
            form._get<ComboBox<Employee>> { id = "manager" }._value = employee
            form._get<DatePicker> { id = "hireDate" }._value = LocalDate.now()
            form._get<IntegerField> { id = "salary" }._value = 25
            val department = Departments.findAll()[0]
            form._get<ComboBox<Department>> { id = "department" }._value = department
            val bean = Employee {}
            form.binder.writeBean(bean)
            expect(Employee {
                name = "Foo"; job = "Bar"; this.departmentId = department.id; managerId = employee.id
                hireDate = LocalDate.now(); salary = 25
            }) { bean }
        }

        @Test
        fun emptyFormWontValidate() {
            expect(false) { form.binder.writeBeanIfValid(Employee {}) }
        }
    }
}