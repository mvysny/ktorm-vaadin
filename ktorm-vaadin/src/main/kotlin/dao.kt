package com.github.mvysny.ktormvaadin

import org.ktorm.entity.add
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table

/**
 * Creates new row in table. Example of use:
 * ```
 * Categories.create(Category { name = "foo" })
 * ```
 */
fun <E : ActiveEntity<E>> Table<E>.create(entity: E): E {
    entity.validate()
    db {
        database.sequenceOf(this@create).add(entity)
    }
    return entity
}
