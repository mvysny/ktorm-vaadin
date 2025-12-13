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
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.masterdetaillayout.MasterDetailLayout
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
    private lateinit var masterDetail: MasterDetailLayout
    private var previewForm: DepartmentForm? = null

    val root = ui {
        verticalLayout {
            setSizeFull()
            h2("Departments")
            masterDetail = masterDetailLayout {
                setWidthFull(); isExpand = true
                detailSize = "400px"
                master {
                    grid<Department>(dataProvider) {
                        setSizeFull()
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
                        asSingleSelect().addValueChangeListener { showDetail(it.value) }
                    }
                }
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

    private fun edit(bean: Department) {
        EntityEditDialog(bean, "department ${bean.name}", DepartmentForm()) {
            dataProvider.refreshAll()
        } .open()
    }

    private fun showDetail(dept: Department?) {
        if (dept == null) {
            masterDetail.detail = null
            previewForm = null
            return
        }
        if (previewForm == null) {
            previewForm = DepartmentForm().apply { binder.setReadOnly(true) }
            masterDetail.detail {
                verticalLayout {
                    add(previewForm)
                    button("Edit") {
                        onClick { edit(previewForm!!.binder.bean) }
                    }
                }
            }
        }
        previewForm!!.binder.bean = dept
    }
}

/**
 * Edits the [Department] bean.
 */
class DepartmentForm : FormLayout(), HasBinder<Department> {
    override val binder = beanValidationBinder<Department>()
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

@VaadinDsl
fun (@VaadinDsl HasComponents).masterDetailLayout(block: (@VaadinDsl MasterDetailLayout).() -> Unit = {}): MasterDetailLayout
        = init(MasterDetailLayout(), block)

@VaadinDsl
fun <C : Component> (@VaadinDsl MasterDetailLayout).master(block: (@VaadinDsl HasComponents).() -> C): C {
    master = buildSingleComponent("MasterDetailLayout.master{}", block)
    return master as C
}
@VaadinDsl
fun <C : Component> (@VaadinDsl MasterDetailLayout).detail(block: (@VaadinDsl HasComponents).() -> C): C {
    detail = buildSingleComponent("MasterDetailLayout.detail{}", block)
    return detail as C
}
