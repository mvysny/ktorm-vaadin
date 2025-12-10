package com.github.mvysny.ktormvaadin

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.ktorm.database.Database

/**
 * Gives access to the singleton [database]. We assume here that most apps will
 * connect to exactly one database.
 */
object ActiveKtorm {
    /**
     * The jakarta.validation validator.
     */
    @Volatile
    var validator: Validator = Validation.buildDefaultValidatorFactory().validator

    /**
     * The [db] function obtains the JDBC connection from here. Use HikariCP connection pooling.
     */
    @Volatile
    lateinit var database: Database
}
