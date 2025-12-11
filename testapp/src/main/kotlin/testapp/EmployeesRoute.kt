package testapp

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.columnFor
import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.h1
import com.github.mvysny.karibudsl.v10.isExpand
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.github.mvysny.kaributools.asc
import com.github.mvysny.kaributools.desc
import com.github.mvysny.kaributools.sort
import com.github.mvysny.ktormvaadin.dataProvider
import com.github.mvysny.ktormvaadin.e
import com.github.mvysny.ktormvaadin.filter.FilterTextField
import com.vaadin.flow.router.Route
import org.ktorm.dsl.and
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.support.postgresql.ilike

@Route("")
class EmployeesRoute : KComposite() {
    private val nameFilter = FilterTextField("name_filter")
    private val jobFilter = FilterTextField("job_filter")
    private val dataProvider = Employees.dataProvider

    val root = ui {
        verticalLayout {
            setSizeFull()
            h1("ktorm-vaadin")
            grid<Employee>(dataProvider) {
                setWidthFull(); isExpand = true
                isMultiSort = true
                appendHeaderRow()
                val filterBar = prependHeaderRow()
                columnFor(Employee::id, key = Employees.id.e.key) {
                    setHeader("ID")
                    isSortable = true
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
                }
                val salaryCol = columnFor(Employee::salary, key = Employees.salary.e.key) {
                    setHeader("Salary")
                    isSortable = true
                }
                sort(nameCol.asc, salaryCol.desc)
            }
        }
    }

    init {
        nameFilter.addValueChangeListener { update() }
        jobFilter.addValueChangeListener { update() }
    }

    private fun update() {
       val conditions = mutableListOf<ColumnDeclaring<Boolean>>()
        if (nameFilter.value.isNotBlank()) {
            conditions += Employees.name.ilike(nameFilter.value.trim() + "%")
        }
        if (jobFilter.value.isNotBlank()) {
            conditions += Employees.job.ilike(jobFilter.value.trim() + "%")
        }
        dataProvider.setFilter(conditions.and())
    }
}