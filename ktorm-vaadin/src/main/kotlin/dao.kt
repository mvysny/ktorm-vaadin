package com.github.mvysny.ktormvaadin

import org.ktorm.dsl.deleteAll
import org.ktorm.entity.Entity
import org.ktorm.entity.add
import org.ktorm.entity.count
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.single
import org.ktorm.entity.toList
import org.ktorm.schema.Table

/**
 * Creates new row in table. Example of use:
 * ```
 * Categories.create(Category { name = "foo" })
 * ```
 */
fun <E : ActiveEntity<E>> Table<E>.create(entity: E): E {
    db {
        database.sequenceOf(this@create).add(entity)
    }
    return entity
}

/**
 * Returns all rows of a table. Don't use on large tables.
 */
fun <E : Entity<E>> Table<E>.findAll(): List<E> = db {
    database.sequenceOf(this@findAll).toList()
}

/**
 * Returns the single row in the table. Fails if the table has 0 or 2+ rows.
 */
fun <E : Entity<E>> Table<E>.single(): E = db {
    database.sequenceOf(this@single).single()
}

/**
 * Deletes all entities from the table.
 */
fun <E : Entity<E>> Table<E>.deleteAll() {
    db {
        database.deleteAll(this@deleteAll)
    }
}

/**
 * Returns the number of rows in this table.
 * @return number of rows, 0 or higher.
 */
fun <E : Entity<E>> Table<E>.count(): Int = db { database.sequenceOf(this@count).count() }
