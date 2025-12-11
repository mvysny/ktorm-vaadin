package testapp

import com.github.mvysny.karibudsl.v10.drawer
import com.github.mvysny.karibudsl.v10.drawerToggle
import com.github.mvysny.karibudsl.v10.h1
import com.github.mvysny.karibudsl.v10.navbar
import com.github.mvysny.karibudsl.v23.route
import com.github.mvysny.karibudsl.v23.sideNav
import com.vaadin.flow.component.applayout.AppLayout
// Main Layout which adds nice side-drawer with navigation
class MainLayout : AppLayout() {
    init {
        navbar {
            drawerToggle()
            h1("Ktorm Vaadin Demo")
        }
        drawer {
            sideNav {
                route(DepartmentsRoute::class, label = "Departments")
                route(EmployeesRoute::class, label = "Employees")
            }
        }
    }
}