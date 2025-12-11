package testapp

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.columnFor
import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.h1
import com.github.mvysny.karibudsl.v10.isExpand
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.github.mvysny.ktormvaadin.dataProvider
import com.github.mvysny.ktormvaadin.key
import com.vaadin.flow.router.Route

@Route("")
class EmployeesRoute : KComposite() {
    val root = ui {
        verticalLayout {
            setSizeFull()
            h1("ktorm-vaadin")
            grid<Employee>(Employees.dataProvider) {
                setWidthFull(); isExpand = true
                columnFor(Employee::id, key = Employees.id.key) {
                    setHeader("ID")
                    isSortable = true
                }
                columnFor(Employee::name, key = Employees.name.key) {
                    setHeader("Name")
                    isSortable = true
                }
                columnFor(Employee::job, key = Employees.job.key) {
                    setHeader("Job")
                    isSortable = true
                }
                columnFor(Employee::hireDate, key = Employees.hireDate.key) {
                    setHeader("Hire date")
                    isSortable = true
                }
                columnFor(Employee::salary, key = Employees.salary.key) {
                    setHeader("Salary")
                    isSortable = true
                }
            }
        }
    }
}