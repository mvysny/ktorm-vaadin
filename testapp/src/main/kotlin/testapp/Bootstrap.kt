package testapp

import com.vaadin.flow.component.page.AppShellConfigurator
import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import jakarta.servlet.annotation.WebListener
import org.slf4j.LoggerFactory

class AppShell : AppShellConfigurator

@WebListener
class Bootstrap : ServletContextListener {
    /**
     * Initializes the application.
     * @param servletContextEvent unused
     */
    @Override
    override fun contextInitialized(servletContextEvent: ServletContextEvent?) {
        log.info("Starting up");
        log.info("Started")
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        log.info("Shutting down")
        log.info("Destroyed")
    }

    companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(Bootstrap::class.java)
    }
}
