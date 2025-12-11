package testapp

import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.kaributools.navigateTo
import com.vaadin.flow.component.grid.Grid
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EmployeesRouteTest {
    companion object {
        private lateinit var routes: Routes
        @BeforeAll
        @JvmStatic
        fun setupApp() {
            routes = Routes().autoDiscoverViews("testapp")
            Bootstrap().contextInitialized(null)
        }

        @AfterAll
        @JvmStatic
        fun teardownApp() {
            Bootstrap().contextDestroyed(null)
        }
    }

    @BeforeEach
    fun setupVaadin() {
        MockVaadin.setup(routes)
    }

    @AfterEach
    fun teardownVaadin() {
        MockVaadin.tearDown()
    }

    @Test
    fun smoke() {
        navigateTo<EmployeesRoute>()
        _expectOne<EmployeesRoute>()
        _get<Grid<Employee>>().expectRows(112)
    }

    @Test fun testSorting() {
        _get<Grid<Employee>>().expectRow(0, "12", "Employee 0", "Employee", "2025-11-12", "6000")
    }
}