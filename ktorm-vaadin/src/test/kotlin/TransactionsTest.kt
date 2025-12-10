package com.github.mvysny.ktormvaadin

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.expect

class TransactionsTest : AbstractDbTest() {
    @BeforeEach fun prepareTestData() {
        db {
            ddl("create table foo (id int not null primary key auto_increment, name varchar(255))")
        }
    }
    @AfterEach fun tearDownTestData() {
        db {
            ddl("drop table if exists foo")
        }
    }

    @Test fun dbCommits() {
        db {
            ddl("insert into foo (name) values ('foo')")
        }
        db {
            expect(1) { sql("select count(*) from foo") { row -> row.getInt(1) } .first() }
        }
    }

    @Test fun dbRollsBack() {
        assertThrows<RuntimeException> {
            db {
                ddl("insert into foo (name) values ('foo')")
                throw RuntimeException("simulated")
            }
        }
        db {
            expect(0) { sql("select count(*) from foo") { row -> row.getInt(1) } .first() }
        }
    }
}