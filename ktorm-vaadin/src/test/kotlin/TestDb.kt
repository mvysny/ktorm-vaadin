package com.github.mvysny.ktormvaadin

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.h2.Driver
import org.ktorm.database.Database

private lateinit var dataSource: HikariDataSource
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

fun tearDownDatabase() {
    dataSource.close()
}