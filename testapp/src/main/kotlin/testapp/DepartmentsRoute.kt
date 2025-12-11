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
import com.github.mvysny.ktormvaadin.and
import com.github.mvysny.ktormvaadin.dataProvider
import com.github.mvysny.ktormvaadin.e
import com.github.mvysny.ktormvaadin.filter.FilterTextField
import com.github.mvysny.ktormvaadin.filter.NumberRangePopup
import com.github.mvysny.ktormvaadin.filter.between
import com.vaadin.flow.router.Route
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.support.postgresql.ilike

@Route("departments")
class DepartmentsRoute : KComposite() {
    private val idFilter = NumberRangePopup()
    private val nameFilter = FilterTextField("name_filter")
    private val locationFilter = FilterTextField("location_filter")
    private val dataProvider = Departments.dataProvider

    val root = ui {
        verticalLayout {
            setSizeFull()
            h1("Departments")
            grid<Department>(dataProvider) {
                setWidthFull(); isExpand = true
                isMultiSort = true
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
        if (nameFilter.value.isNotBlank()) {
            conditions += Employees.name.ilike(nameFilter.value.trim() + "%")
        }
        if (locationFilter.value.isNotBlank()) {
            conditions += Employees.job.ilike(locationFilter.value.trim() + "%")
        }
        dataProvider.setFilter(conditions.and())
    }
}