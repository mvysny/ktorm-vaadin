package testapp

import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.kaributools.navigateTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.expect

class MainRouteTest {
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
        navigateTo<MainRoute>()
        _expectOne<MainRoute>()
    }
}