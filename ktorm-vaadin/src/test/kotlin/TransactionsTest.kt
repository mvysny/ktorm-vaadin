package com.github.mvysny.ktormvaadin

import com.github.mvysny.kaributesting.v10.expectList
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.expect

class TransactionsTest : AbstractDbTest() {
    @BeforeEach fun prepareTestData() {
        db { ddl("create table foo (id int not null primary key auto_increment, name varchar(255))") }
    }

    @AfterEach fun tearDownTestData() {
        db { ddl("drop table if exists foo") }
    }

    @Test
    fun dbCommits() {
        db {
            ddl("insert into foo (name) values ('foo')")
        }
        db {
            expect(1) { sql("select count(*) from foo") { row -> row.getInt(1) }.first() }
        }
    }

    @Test
    fun nestedTransactions() {
        db {
            db {
                db {
                    ddl("insert into foo (name) values ('foo')")
                }
            }
        }
        db {
            expect(1) { sql("select count(*) from foo") { row -> row.getInt(1) }.first() }
        }
    }

    @Test
    fun sqlWithParameters() {
        db {
            ddl("insert into foo (name) values ('foo')")
            ddl("insert into foo (name) values ('bar')")
        }
        db {
            expectList("bar") {
                sql("select name from foo where name = ?", { setString(1, "bar") }) { row -> row.getString(1) }
            }
            expectList {
                sql("select name from foo where name = ?", { setString(1, "n/a") }) { row -> row.getString(1) }
            }
        }
    }

    @Test
    fun dbRollsBack() {
        assertThrows<RuntimeException> {
            db {
                ddl("insert into foo (name) values ('foo')")
                throw RuntimeException("simulated")
            }
        }
        db {
            expect(0) { sql("select count(*) from foo") { row -> row.getInt(1) }.first() }
        }
    }
}