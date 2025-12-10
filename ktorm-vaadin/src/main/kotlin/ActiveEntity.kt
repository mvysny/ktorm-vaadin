package com.github.mvysny.ktormvaadin

import jakarta.validation.ConstraintViolationException
import org.ktorm.entity.Entity
import org.ktorm.schema.Column
import org.ktorm.schema.NestedBinding
import org.ktorm.schema.Table

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
        return table.primaryKeys.any { get(it.propertyName) != null }
    }

    /**
     * Saves changes done in this entity to the database, or creates a new row if the entity has no ID.
     */
    fun save(validate: Boolean = true) {
        if (validate) {
            validate()
        }
        if (hasId) {
            flushChanges()
        } else {
            create(false)
        }
    }

    /**
     * Creates a new row. Shouldn't be called if the entity already has an ID.
     */
    fun create(validate: Boolean = true) {
        if (validate) {
            validate()
        }
        table.create(this as E)
    }
}

val Column<*>.propertyName: String
    get() {
        val b =
            checkNotNull(binding) { "$this is not bound to an entity, can't retrieve entity's property name of this column" }
        check(b is NestedBinding) { "$this: unsupported binding $b" }
        return b.properties.last().name
    }