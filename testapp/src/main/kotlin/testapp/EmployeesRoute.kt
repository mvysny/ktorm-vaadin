package testapp

import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.asc
import com.github.mvysny.kaributools.desc
import com.github.mvysny.kaributools.sort
import com.github.mvysny.ktormvaadin.QueryDataProvider
import com.github.mvysny.ktormvaadin.and
import com.github.mvysny.ktormvaadin.bind
import com.github.mvysny.ktormvaadin.dataProvider
import com.github.mvysny.ktormvaadin.filter.DateRangePopup
import com.github.mvysny.ktormvaadin.filter.FilterTextField
import com.github.mvysny.ktormvaadin.filter.NumberRangePopup
import com.github.mvysny.ktormvaadin.filter.between
import com.github.mvysny.ktormvaadin.q
import com.github.mvysny.ktormvaadin.toId
import com.github.mvysny.ktormvaadin.withStringFilterOn
import com.vaadin.flow.component.ItemLabelGenerator
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.converter.IntegerToLongConverter
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
 */
@Route("", layout = MainLayout::class)
class EmployeesRoute : KComposite() {
    private val idFilter = NumberRangePopup()
    private val nameFilter = FilterTextField("name_filter")
    private val jobFilter = FilterTextField("job_filter")
    private val hireDateFilter = DateRangePopup()
    private val salaryFilter = NumberRangePopup()
    private val deptFilter = FilterTextField("dept_filter")
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
                column({ it.employee.id }) {
                    setHeader("ID")
                    key = Employees.id.q.key
                    isSortable = true
                    filterBar.getCell(this).component = idFilter
                }
                val nameCol = column({ it.employee.name }) {
                    setHeader("Name")
                    key = Employees.name.q.key
                    isSortable = true
                    filterBar.getCell(this).component = nameFilter
                }
                column({ it.employee.job}) {
                    setHeader("Job")
                    key = Employees.job.q.key
                    isSortable = true
                    filterBar.getCell(this).component = jobFilter
                }
                column({ it.employee.hireDate }) {
                    setHeader("Hire date")
                    key = Employees.hireDate.q.key
                    isSortable = true
                    filterBar.getCell(this).component = hireDateFilter
                }
                val salaryCol = column({ it.employee.salary }) {
                    setHeader("Salary")
                    key = Employees.salary.q.key
                    isSortable = true
                    filterBar.getCell(this).component = salaryFilter
                }
                column({ it.department.name }) {
                    setHeader("Department")
                    key = Departments.name.q.key
                    isSortable = true
                    filterBar.getCell(this).component = deptFilter
                }
                sort(nameCol.asc, salaryCol.desc)
                addItemClickListener { if (it.item != null) edit(it.item) }
            }
        }
    }

    init {
        idFilter.addValueChangeListener { update() }
        nameFilter.addValueChangeListener { update() }
        jobFilter.addValueChangeListener { update() }
        hireDateFilter.addValueChangeListener { update() }
        salaryFilter.addValueChangeListener { update() }
        deptFilter.addValueChangeListener { update() }
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
        if (deptFilter.value.isNotBlank()) {
            conditions += Departments.name.ilike(deptFilter.value.trim() + "%")
        }
        dataProvider.setFilter(conditions.and())
    }

    private fun edit(bean: EmployeeDept) {
        EntityEditDialog(bean.employee, "employee ${bean.employee.name}", EmployeeForm()) {
            dataProvider.refreshAll()
        } .open()
    }
}

/**
 * A bean which captures a join of [Employee] and [Department].
 */
data class EmployeeDept(val employee: Employee, val department: Department) {
    companion object {
        fun from(row: QueryRowSet): EmployeeDept = EmployeeDept(
            Employees.createEntity(row), Departments.createEntity(row)
        )
        val dataProvider: QueryDataProvider<EmployeeDept> get() = QueryDataProvider<EmployeeDept>(
            { it.from(Employees).leftJoin(Departments, on = Employees.departmentId eq Departments.id)
                .select(*Employees.columns.toTypedArray(), *Departments.columns.toTypedArray()) },
            { from(it) }
        )
    }
}

/**
 * A form which edits an [Employee]. Uses [binder] to do that.
 * Read Vaadin documentation on Binder to understand the concept.
 */
class EmployeeForm : FormLayout(), HasBinder<Employee> {
   override val binder = beanValidationBinder<Employee>()
    init {
        textField("Name") {
            setId("name")
            bind(binder).bind(Employees.name)
        }
        textField("Job") {
            setId("job")
            bind(binder).bind(Employees.job)
        }
        comboBox<Employee>("Manager") {
            setId("manager")
            setItems(Employees.dataProvider.withStringFilterOn(Employees.name))
            itemLabelGenerator = ItemLabelGenerator { it.name }
            bind(binder).toId(Employees.id).bind(Employees.managerId)
        }
        datePicker("Hire Date") {
            setId("hireDate")
            bind(binder).bind(Employees.hireDate)
        }
        integerField("Salary") {
            setId("salary")
            bind(binder).withConverter(IntegerToLongConverter()).bind(Employees.salary)
        }
        comboBox<Department>("Department") {
            setId("department")
            setItems(Departments.dataProvider.withStringFilterOn(Departments.name))
            itemLabelGenerator = ItemLabelGenerator { it.name }
            bind(binder).toId(Departments.id).bind(Employees.departmentId)
        }
    }
}