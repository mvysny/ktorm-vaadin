package com.github.mvysny.ktormvaadin

import com.vaadin.flow.data.binder.Result
import com.vaadin.flow.data.binder.ValueContext
import com.vaadin.flow.data.converter.Converter
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.singleOrNull
import org.ktorm.schema.Column
import org.ktorm.schema.Table
import kotlin.reflect.KClass

/**
 * Converts an entity to its ID and back. Useful for combo boxes which shows a list of entities as their options while being bound to a
 * field containing ID of that entity.
 * @param T the type of the entity
 * @param ID the type of the ID field of the entity
 */
class EntityToIdConverter<ID: Any, T: Entity<T>>(val idColumn: Column<ID>, val entityClass: KClass<T>) : Converter<T?, ID?> {
    init {
        require(idColumn.table.entityClass == entityClass) {
            "The idColumn $idColumn belongs to a table which produces ${idColumn.table.entityClass} but $entityClass was expected"
        }
    }

    override fun convertToModel(value: T?, context: ValueContext?): Result<ID?> =
        Result.ok(value?.get(idColumn.property.name) as ID?)

    override fun convertToPresentation(value: ID?, context: ValueContext?): T? {
        if (value == null) return null
        return db {
            database.sequenceOf(idColumn.table as Table<T>).singleOrNull { idColumn.eq(value) }
        }
    }
}
