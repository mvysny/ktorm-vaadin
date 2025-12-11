package testapp

import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.asc
import com.github.mvysny.kaributools.desc
import com.github.mvysny.kaributools.sort
import com.github.mvysny.ktormvaadin.QueryDataProvider
import com.github.mvysny.ktormvaadin.and
import com.github.mvysny.ktormvaadin.filter.DateRangePopup
import com.github.mvysny.ktormvaadin.filter.FilterTextField
import com.github.mvysny.ktormvaadin.filter.NumberRangePopup
import com.github.mvysny.ktormvaadin.filter.between
import com.github.mvysny.ktormvaadin.q
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.router.Route
import org.ktorm.dsl.QueryRowSet
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.select
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.support.postgresql.ilike

/**
 * Shows employees. Demoes [com.github.mvysny.ktormvaadin.QueryDataProvider].
 * TODO reimplement using QueryDataProvider
 */
@Route("", layout = MainLayout::class)
class EmployeesRoute : KComposite() {
    private val idFilter = NumberRangePopup()
    private val nameFilter = FilterTextField("name_filter")
    private val jobFilter = FilterTextField("job_filter")
    private val hireDateFilter = DateRangePopup()
    private val salaryFilter = NumberRangePopup()
    private val dataProvider = EmployeeDept.dataProvider

    val root = ui {
        verticalLayout {
            setSizeFull()
            h2("Employees")
            grid<EmployeeDept>(dataProvider) {
                setWidthFull(); isExpand = true
                setMultiSort(true, Grid.MultiSortPriority.APPEND, true)
                appendHeaderRow()
                val filterBar = prependHeaderRow()
                column({ it.e.id }) {
                    setHeader("ID")
                    key = Employees.id.q.key
                    isSortable = true
                    filterBar.getCell(this).component = idFilter
                }
                val nameCol = column({ it.e.name }) {
                    setHeader("Name")
                    key = Employees.name.q.key
                    isSortable = true
                    filterBar.getCell(this).component = nameFilter
                }
                column({ it.e.job}) {
                    setHeader("Job")
                    key = Employees.job.q.key
                    isSortable = true
                    filterBar.getCell(this).component = jobFilter
                }
                column({ it.e.hireDate }) {
                    setHeader("Hire date")
                    key = Employees.hireDate.q.key
                    isSortable = true
                    filterBar.getCell(this).component = hireDateFilter
                }
                val salaryCol = column({ it.e.salary }) {
                    setHeader("Salary")
                    key = Employees.salary.q.key
                    isSortable = true
                    filterBar.getCell(this).component = salaryFilter
                }
                sort(nameCol.asc, salaryCol.desc)
            }
        }
    }

    init {
        idFilter.addValueChangeListener { update() }
        nameFilter.addValueChangeListener { update() }
        jobFilter.addValueChangeListener { update() }
        hireDateFilter.addValueChangeListener { update() }
        salaryFilter.addValueChangeListener { update() }
        idFilter.addValueChangeListener { update() }
    }

    private fun update() {
       val conditions = mutableListOf<ColumnDeclaring<Boolean>?>()
        conditions += Employees.id.between(idFilter.value.asIntegerInterval())
        if (nameFilter.value.isNotBlank()) {
            conditions += Employees.name.ilike(nameFilter.value.trim() + "%")
        }
        if (jobFilter.value.isNotBlank()) {
            conditions += Employees.job.ilike(jobFilter.value.trim() + "%")
        }
        conditions += Employees.hireDate.between(hireDateFilter.value)
        conditions += Employees.salary.between(salaryFilter.value.asLongInterval())
        dataProvider.setFilter(conditions.and())
    }
}

data class EmployeeDept(val e: Employee, val d: Department) {
    companion object {
        fun from(row: QueryRowSet): EmployeeDept = EmployeeDept(
            Employees.createEntity(row), Departments.createEntity(row)
        )
        val dataProvider: QueryDataProvider<EmployeeDept> get() = QueryDataProvider<EmployeeDept>(
            listOf(Employees, Departments),
            { it.from(Employees).leftJoin(Departments, on = Employees.departmentId eq Departments.id) },
            { it.select(*Employees.columns.toTypedArray(), *Departments.columns.toTypedArray()) },
            { from(it) }
        )
    }
}