package testapp

import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.kaributools.navigateTo
import com.github.mvysny.ktormvaadin.filter.BooleanFilterField
import com.github.mvysny.ktormvaadin.filter.DateInterval
import com.github.mvysny.ktormvaadin.filter.DateRangePopup
import com.github.mvysny.ktormvaadin.filter.EnumFilterField
import com.github.mvysny.ktormvaadin.filter.NumberInterval
import com.github.mvysny.ktormvaadin.filter.NumberRangePopup
import com.github.mvysny.ktormvaadin.findAll
import com.vaadin.flow.component.checkbox.Checkbox
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
        _get<Grid<Employee>>().expectRowRegex(0, "\\d+", "Manager 1", "Manager", "2025-11-12", "6001", "Dept .*", "Married", "false")
        _get<Grid<Employee>>().expectRows(2)
    }

    @Test
    fun testJobFilter() {
        _get<TextField> { id = "job_filter" }._value = "Manager"
        _get<Grid<Employee>>().expectRowRegex(0, "\\d+", "Manager 0", "Manager", "2025-11-12", "6000", "Dept .*", "Single", "true")
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

    /**
     * The demo data: 11 managers with salaries 6000..6010 and 101 employees with
     * salaries 6000..6100.
     */
    @Test
    fun testSalaryFilter() {
        val salaryFilter = _get<NumberRangePopup> { id = "salary_filter" }
        salaryFilter._value = NumberInterval(6000.0, 6000.0)
        _get<Grid<Employee>>().expectRows(2)
        salaryFilter._value = NumberInterval(6000.0, 6005.0)
        _get<Grid<Employee>>().expectRows(12)
        salaryFilter._value = NumberInterval(6095.0, null)
        _get<Grid<Employee>>().expectRows(6)
        salaryFilter._value = NumberInterval(null, 6000.0)
        _get<Grid<Employee>>().expectRows(2)
        salaryFilter._value = NumberInterval(null, null)
        _get<Grid<Employee>>().expectRows(112)
    }

    @Test
    fun testHireDateFilter() {
        val hireDate = LocalDate.of(2025, 11, 12)
        val hireDateFilter = _get<DateRangePopup> { id = "hiredate_filter" }
        hireDateFilter._value = DateInterval.of(hireDate)
        _get<Grid<Employee>>().expectRows(112)
        hireDateFilter._value = DateInterval(hireDate.plusDays(1), null)
        _get<Grid<Employee>>().expectRows(0)
        hireDateFilter._value = DateInterval(null, hireDate.minusDays(1))
        _get<Grid<Employee>>().expectRows(0)
        hireDateFilter._value = DateInterval(hireDate.minusDays(1), hireDate.plusDays(1))
        _get<Grid<Employee>>().expectRows(112)
    }

    @Test
    fun testIdFilter() {
        val ids = Employees.findAll().map { it.id } .sorted()
        _get<NumberRangePopup> { id = "id_filter" }._value =
            NumberInterval(ids[0].toDouble(), ids[4].toDouble())
        _get<Grid<Employee>>().expectRows(5)
    }

    /**
     * The demo data cycles through the marital statuses: 38 Single, 38 Married, 36 Divorced.
     */
    @Test
    fun testMaritalStatusFilter() {
        val filter = _get<EnumFilterField<MaritalStatus>> { id = "marital_status_filter" }
        filter.select(MaritalStatus.Married)
        _get<Grid<Employee>>().expectRows(38)
        filter.select(MaritalStatus.Divorced)
        _get<Grid<Employee>>().expectRows(74)
        // selecting every constant filters nothing out
        filter.select(MaritalStatus.Single)
        _get<Grid<Employee>>().expectRows(112)
        filter.deselectAll()
        _get<Grid<Employee>>().expectRows(112)
    }

    /**
     * Every second employee in the demo data works remotely: 57 of them.
     */
    @Test
    fun testRemoteFilter() {
        val filter = _get<BooleanFilterField> { id = "remote_filter" }
        filter._value = true
        _get<Grid<Employee>>().expectRows(57)
        filter._value = false
        _get<Grid<Employee>>().expectRows(55)
        // null means "don't filter"
        filter._value = null
        _get<Grid<Employee>>().expectRows(112)
    }

    @Test
    fun testRemoteAndMaritalStatusFilter() {
        _get<BooleanFilterField> { id = "remote_filter" }._value = true
        _get<EnumFilterField<MaritalStatus>> { id = "marital_status_filter" }.select(MaritalStatus.Married)
        _get<Grid<Employee>>().expectRows(19)
    }

    /**
     * All filters are ANDed together.
     */
    @Test
    fun testSalaryAndNameFilter() {
        _get<NumberRangePopup> { id = "salary_filter" }._value = NumberInterval(6000.0, 6005.0)
        _get<TextField> { id = "name_filter" }._value = "Manager"
        _get<Grid<Employee>>().expectRows(6)
    }

    @Test
    fun testSortingSmoke() {
        val grid = _get<Grid<Employee>>()
        grid.expectRowRegex(0, "\\d+", "Employee 0", "Employee", "2025-11-12", "6000", "Dept .*", "Single", "true")
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
            form._get<ComboBox<MaritalStatus>> { id = "maritalStatus" }._value = MaritalStatus.Married
            form._get<Checkbox> { id = "remote" }._value = true
            val department = Departments.findAll()[0]
            form._get<ComboBox<Department>> { id = "department" }._value = department
            val bean = Employee {}
            form.binder.writeBean(bean)
            expect(Employee {
                name = "Foo"; job = "Bar"; this.departmentId = department.id; managerId = employee.id
                hireDate = LocalDate.now(); salary = 25
                maritalStatus = MaritalStatus.Married; remote = true
            }) { bean }
        }

        @Test
        fun emptyFormWontValidate() {
            expect(false) { form.binder.writeBeanIfValid(Employee {}) }
        }
    }
}