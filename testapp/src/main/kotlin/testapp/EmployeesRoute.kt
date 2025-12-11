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
import com.vaadin.flow.router.Route

@Route("")
class EmployeesRoute : KComposite() {
    val root = ui {
        verticalLayout {
            setSizeFull()
            h1("ktorm-vaadin")
            grid<Employee>(Employees.dataProvider) {
                setWidthFull(); isExpand = true
                isMultiSort = true
                columnFor(Employee::id, key = Employees.id.e.key) {
                    setHeader("ID")
                    isSortable = true
                }
                val nameCol = columnFor(Employee::name, key = Employees.name.e.key) {
                    setHeader("Name")
                    isSortable = true
                }
                columnFor(Employee::job, key = Employees.job.e.key) {
                    setHeader("Job")
                    isSortable = true
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
}