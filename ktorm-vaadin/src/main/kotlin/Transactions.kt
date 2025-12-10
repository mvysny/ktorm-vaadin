package com.github.mvysny.ktormvaadin

import org.ktorm.database.Database
import org.ktorm.database.Transaction
import org.ktorm.database.asIterable
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Makes sure given block is executed in a DB transaction. When the block finishes normally, the transaction commits;
 * if the block throws any exception, the transaction is rolled back.
 *
 * Example of use: `db { ddl("yada yada") }`
 * @param block the block to run in the transaction.
 */
fun <R> db(block: KtormContext.() -> R): R =
    ActiveKtorm.database.useTransaction {
        KtormContext(
            it,
            ActiveKtorm.database
        ).block()
    }

/**
 * The database context, provides access to [transaction] and Ktorm [database] object
 * which you use to run SQL.
 */
data class KtormContext(val transaction: Transaction, val database: Database)

/**
 * Runs a SQL update statement, or a DDL SQL script. Beware of SQL injections etc.
 * @param ddl the DDL SQL script to run.
 */
fun Transaction.ddl(ddl: String) {
    connection.prepareStatement(ddl).executeUpdate()
}

/**
 * Runs a SQL update statement, or a DDL SQL script. Beware of SQL injections etc.
 * @param ddl the DDL SQL script to run.
 */
fun KtormContext.ddl(ddl: String) {
    transaction.ddl(ddl)
}

/**
 * Runs native SQL select. Make sure to defend against SQL injection, etc etc. Example:
 * ```
 * sql("select sum(r.count) from Review r where r.category = ?", { setLong(1, categoryId) }) { it.getLong(1) } .firstOrNull() ?: 0L
 * ```
 * @param sql the SQL to run.
 * @param parameters (optional) sets parameters to [PreparedStatement]
 * @param mapper maps [ResultSet] row to the bean [T]
 * @param [T] the type of returned bean
 * @return list of beans found.
 */
fun <T> KtormContext.sql(sql: String, parameters: PreparedStatement.() -> Unit = {}, mapper: (ResultSet) -> T): List<T> =
    transaction.connection.prepareStatement(sql).use { statement ->
        parameters(statement)
        statement.executeQuery().asIterable().map(mapper)
    }