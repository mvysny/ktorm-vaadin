package testapp

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.columnFor
import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.h1
import com.github.mvysny.karibudsl.v10.isExpand
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.github.mvysny.ktormvaadin.findAll
import com.vaadin.flow.router.Route

@Route("")
class MainRoute : KComposite() {
    val root = ui {
        verticalLayout {
            setSizeFull()
            h1("ktorm-vaadin")
            grid<Employee> {
                setWidthFull(); isExpand = true
                columnFor(Employee::id) {
                    setHeader("ID")
                }
                columnFor(Employee::name) {
                    setHeader("Name")
                }
                columnFor(Employee::job) {
                    setHeader("Job")
                }
                columnFor(Employee::hireDate) {
                    setHeader("Hire date")
                }
                columnFor(Employee::salary) {
                    setHeader("Salary")
                }
                setItems(Employees.findAll()) // todo use DataProvider
            }
        }
    }
}