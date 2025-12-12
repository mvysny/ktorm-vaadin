package testapp

import com.github.mvysny.karibudsl.v10.*
import com.github.mvysny.kaributools.asc
import com.github.mvysny.kaributools.sort
import com.github.mvysny.ktormvaadin.and
import com.github.mvysny.ktormvaadin.bind
import com.github.mvysny.ktormvaadin.dataProvider
import com.github.mvysny.ktormvaadin.e
import com.github.mvysny.ktormvaadin.filter.FilterTextField
import com.github.mvysny.ktormvaadin.filter.NumberRangePopup
import com.github.mvysny.ktormvaadin.filter.between
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.support.postgresql.ilike

/**
 * shows departments. Demoes [com.github.mvysny.ktormvaadin.EntityDataProvider]
  */
@Route("departments", layout = MainLayout::class)
class DepartmentsRoute : KComposite() {
    private val idFilter = NumberRangePopup()
    private val nameFilter = FilterTextField("name_filter")
    private val locationFilter = FilterTextField("location_filter")
    private val dataProvider = Departments.dataProvider

    val root = ui {
        verticalLayout {
            setSizeFull()
            h2("Departments")
            grid<Department>(dataProvider) {
                setWidthFull(); isExpand = true
                setMultiSort(true, Grid.MultiSortPriority.APPEND, true)
                appendHeaderRow()
                val filterBar = prependHeaderRow()
                columnFor(Department::id, key = Departments.id.e.key) {
                    setHeader("ID")
                    isSortable = true
                    filterBar.getCell(this).component = idFilter
                }
                val nameCol = columnFor(Department::name, key = Departments.name.e.key) {
                    setHeader("Name")
                    isSortable = true
                    filterBar.getCell(this).component = nameFilter
                }
                columnFor(Department::location, key = Departments.location.e.key) {
                    setHeader("Location")
                    isSortable = true
                    filterBar.getCell(this).component = locationFilter
                }
                sort(nameCol.asc)
            }
        }
    }

    init {
        idFilter.addValueChangeListener { update() }
        nameFilter.addValueChangeListener { update() }
        locationFilter.addValueChangeListener { update() }
    }

    private fun update() {
       val conditions = mutableListOf<ColumnDeclaring<Boolean>?>()
        conditions += Departments.id.between(idFilter.value.asIntegerInterval())
        if (nameFilter.value.isNotBlank()) {
            conditions += Departments.name.ilike(nameFilter.value.trim() + "%")
        }
        if (locationFilter.value.isNotBlank()) {
            conditions += Departments.location.ilike(locationFilter.value.trim() + "%")
        }
        dataProvider.setFilter(conditions.and())
    }
}

/**
 * Edits the [Department] bean.
 */
class DepartmentForm : FormLayout() {
    val binder = beanValidationBinder<Department>()
    init {
        textField("Name") {
            setId("name")
            bind(binder).bind(Departments.name)
        }
        textField("Location") {
            setId("location")
            bind(binder).bind(Departments.location)
        }
    }
}