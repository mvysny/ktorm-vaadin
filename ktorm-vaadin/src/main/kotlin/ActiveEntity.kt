package com.github.mvysny.ktormvaadin

import jakarta.validation.ConstraintViolationException
import org.ktorm.entity.Entity
import org.ktorm.schema.Column
import org.ktorm.schema.NestedBinding
import org.ktorm.schema.ReferenceBinding
import org.ktorm.schema.Table
import kotlin.reflect.KProperty1

/**
 * An active entity which is able to:
 * - validate itself via the [validate] function. It uses the standardized
 * JSR303 `jakarta.validation`, which means that the POJOs are directly compatible with
 * Vaadin's BeanValidationBinder.
 * - [save]/[create] itself
 * - [delete] itself (this comes from Ktorm itself)
 */
interface ActiveEntity<E : ActiveEntity<E>> : Entity<E> {

    /**
     * Validates current entity. The Java JSR303 validation is performed by default: just add `jakarta.validation`
     * annotations to entity properties.
     *
     * Make sure to add the validation annotations to
     * fields or getters otherwise they will be ignored. For example `@field:NotNull` or `@get:NotNull`.
     *
     * You can override this method to perform additional validations on the level of the entire entity.
     *
     * @throws jakarta.validation.ValidationException when validation fails.
     */
    fun validate() {
        val violations = ActiveKtorm.validator.validate<Any>(this)
        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }
    }

    /**
     * The table of this entity.
     */
    val table: Table<E>

    /**
     * Checks whether this entity is valid: calls [validate] and returns false if [ConstraintViolationException] is thrown.
     */
    val isValid: Boolean
        get() = try {
            validate()
            true
        } catch (_: ConstraintViolationException) {
            false
        }

    /**
     * True if this entity has an ID (is saved to the database).
     */
    val hasId: Boolean get() {
        check(table.primaryKeys.isNotEmpty()) { "Primary keys cannot be empty" }
        return table.primaryKeys.any { get(it.property.name) != null }
    }

    /**
     * Saves changes done in this entity to the database, or creates a new row if the entity has no ID.
     * @return this
     */
    fun save(validate: Boolean = true): E {
        if (validate) {
            validate()
        }
        if (hasId) {
            flushChanges()
        } else {
            create(false)
        }
        return self
    }

    val self: E get() = this as E

    /**
     * Creates a new row. Shouldn't be called if the entity already has an ID.
     * @return this
     */
    fun create(validate: Boolean = true): E {
        if (validate) {
            validate()
        }
        table.create(self)
        return self
    }
}

/**
 * Returns the Kotlin [KProperty1] of a bean this column is bound to.
 */
val Column<*>.property: KProperty1<*, *>
    get() = if (binding is ReferenceBinding) {
        (binding as ReferenceBinding).onProperty
    } else {
        val properties = (binding as NestedBinding).properties
        require(properties.size == 1) { "$this: nested properties aren't supported: $binding" }
        properties[0]
    }