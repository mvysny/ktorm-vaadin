package testapp

import com.github.mvysny.ktormvaadin.ActiveKtorm
import com.vaadin.flow.component.page.AppShellConfigurator
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import jakarta.servlet.annotation.WebListener
import org.h2.Driver
import org.ktorm.database.Database
import org.slf4j.LoggerFactory

class AppShell : AppShellConfigurator

@WebListener
class Bootstrap : ServletContextListener {
    // Called by Jetty when the app starts up.
    @Override
    override fun contextInitialized(servletContextEvent: ServletContextEvent?) {
        log.info("Connecting to the database")
        val cfg = HikariConfig().apply {
            driverClassName = Driver::class.java.name
            jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
            username = "sa"
            password = ""
        }
        dataSource = HikariDataSource(cfg)
        ActiveKtorm.database = Database.connect(dataSource)
        log.info("Started")
    }

    // Called by Jetty when the app is stopped.
    override fun contextDestroyed(sce: ServletContextEvent?) {
        log.info("Closing database connections")
        dataSource.close()
        log.info("Destroyed")
    }

    companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(Bootstrap::class.java)
        private lateinit var dataSource: HikariDataSource
    }
}
