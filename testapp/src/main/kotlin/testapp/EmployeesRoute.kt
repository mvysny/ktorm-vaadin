package testapp

import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.asc
import com.github.mvysny.kaributools.desc
import com.github.mvysny.kaributools.sort
import com.github.mvysny.ktormvaadin.and
import com.github.mvysny.ktormvaadin.dataProvider
import com.github.mvysny.ktormvaadin.e
import com.github.mvysny.ktormvaadin.filter.DateRangePopup
import com.github.mvysny.ktormvaadin.filter.FilterTextField
import com.github.mvysny.ktormvaadin.filter.NumberRangePopup
import com.github.mvysny.ktormvaadin.filter.between
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.router.Route
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.support.postgresql.ilike

@Route("", layout = MainLayout::class)
class EmployeesRoute : KComposite() {
    private val idFilter = NumberRangePopup()
    private val nameFilter = FilterTextField("name_filter")
    private val jobFilter = FilterTextField("job_filter")
    private val hireDateFilter = DateRangePopup()
    private val salaryFilter = NumberRangePopup()
    private val dataProvider = Employees.dataProvider

    val root = ui {
        verticalLayout {
            setSizeFull()
            h2("Employees")
            grid<Employee>(dataProvider) {
                setWidthFull(); isExpand = true
                setMultiSort(true, Grid.MultiSortPriority.APPEND, true)
                appendHeaderRow()
                val filterBar = prependHeaderRow()
                columnFor(Employee::id, key = Employees.id.e.key) {
                    setHeader("ID")
                    isSortable = true
                    filterBar.getCell(this).component = idFilter
                }
                val nameCol = columnFor(Employee::name, key = Employees.name.e.key) {
                    setHeader("Name")
                    isSortable = true
                    filterBar.getCell(this).component = nameFilter
                }
                columnFor(Employee::job, key = Employees.job.e.key) {
                    setHeader("Job")
                    isSortable = true
                    filterBar.getCell(this).component = jobFilter
                }
                columnFor(Employee::hireDate, key = Employees.hireDate.e.key) {
                    setHeader("Hire date")
                    isSortable = true
                    filterBar.getCell(this).component = hireDateFilter
                }
                val salaryCol = columnFor(Employee::salary, key = Employees.salary.e.key) {
                    setHeader("Salary")
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