package testapp

import com.github.mvysny.ktormvaadin.ActiveKtorm
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.server.ErrorHandler
import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.SessionInitEvent
import com.vaadin.flow.server.SessionInitListener
import com.vaadin.flow.server.VaadinServiceInitListener
import com.vaadin.flow.theme.lumo.Lumo
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import jakarta.servlet.annotation.WebListener
import org.flywaydb.core.Flyway
import org.ktorm.database.Database
import org.slf4j.LoggerFactory

// Required by Vaadin
@StyleSheet(Lumo.STYLESHEET)
class AppShell : AppShellConfigurator

// All this bullshit, just to show error notification if the app throws.
class MyAppServiceInitListener : VaadinServiceInitListener, SessionInitListener {
    override fun serviceInit(event: ServiceInitEvent) {
        event.source.addSessionInitListener(this)
    }

    override fun sessionInit(event: SessionInitEvent) {
        event.session.errorHandler = ErrorHandler { errorEvent ->
            log.error("Internal error", errorEvent.throwable)
            showErrorNotification("Internal error: ${errorEvent.throwable?.message}")
        }
    }
    companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(MyAppServiceInitListener::class.java)
    }
}

// Called by Jetty before the app starts serving requests, and afterwards when it's killed.
@WebListener
class Bootstrap : ServletContextListener {
    // Called by Jetty when the app starts up.
    @Override
    override fun contextInitialized(servletContextEvent: ServletContextEvent?) {
        log.info("Connecting to the database")
        val cfg = HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
            username = "sa"
            password = ""
        }
        dataSource = HikariDataSource(cfg)
        ActiveKtorm.database = Database.connect(dataSource)
        log.info("Creating tables")
        val flyway: Flyway = Flyway.configure()
            .dataSource(dataSource)
            .load()
        flyway.migrate()
        log.info("Demo data")
        demoData()
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
