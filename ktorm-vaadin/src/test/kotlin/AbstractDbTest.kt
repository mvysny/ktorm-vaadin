package com.github.mvysny.ktormvaadin

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.h2.Driver
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.ktorm.database.Database

abstract class AbstractDbTest {
    companion object {
        private lateinit var dataSource: HikariDataSource
        @JvmStatic
        @BeforeAll
        fun setupDatabase() {
            val cfg = HikariConfig().apply {
                driverClassName = Driver::class.java.name
                jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
                username = "sa"
                password = ""
            }
            dataSource = HikariDataSource(cfg)
            ActiveKtorm.database = Database.connect(dataSource)
        }

        @JvmStatic
        @AfterAll
        fun tearDownDatabase() {
            dataSource.close()
        }
    }
}