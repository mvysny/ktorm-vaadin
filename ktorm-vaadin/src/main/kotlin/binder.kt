package com.github.mvysny.ktormvaadin

import com.vaadin.flow.data.binder.Binder
import org.ktorm.entity.Entity
import org.ktorm.schema.Column
import kotlin.reflect.KProperty1

/**
 * When using [Binder]: bind a form field to entity's [Column].
 * Type-safe: binds only to a property of a correct type.
 * @param column the ktorm column on the [BEAN] entity.
 * @param [BEAN] the bean, usually an [org.ktorm.entity.Entity]
 * @param [FIELDVALUE] the type of the value
 */
fun <BEAN, FIELDVALUE:Any> Binder.BindingBuilder<BEAN, FIELDVALUE?>.bind(column: Column<out FIELDVALUE>): Binder.Binding<BEAN, FIELDVALUE?> {
    val binding: KProperty1<*, *> = column.property

    // oh crap, don't use binding by getter and setter - validations won't work!
    // we need to use bind(String) even though that will use undebuggable crappy Java 8 lambdas :-(
    //        bind({ bean -> prop.get(bean) }, { bean, value -> prop.set(bean, value) })
    var name = binding.name
    if (name.startsWith("is")) {
        // Kotlin KProperties named "isFoo" are represented with just "foo" in the bean property set
        name = name[2].lowercase() + name.drop(3)
    }
    return bind(name)
}

/**
 * Converts an entity to its ID and back. Useful for combo boxes which shows a list of entities as their options while being bound to a
 * field containing ID of that entity:
 * ```kotlin
 * interface Category(override var id: Long? = null, var name: String = "") : Entity<Long>
 * interface Review(override var id: Long? = null, var category: Long? = null) : Entity<Long>
 *
 * // editing the Review, we want the user to be able to choose the Review's category
 * val binder = BeanValidationBinder(Review::class.java)
 * categoryBox = comboBox("Choose a category") {
 *     setItemLabelGenerator { it.name }
 *     isAllowCustomValue = false
 *     dataProvider = Categories.dataProvider
 *     bind(binder).toId().bind(Reviews.category)
 * }
 * ```
 */
inline fun <BEAN, ID: Any, reified ENTITY: Entity<ENTITY>> Binder.BindingBuilder<BEAN, ENTITY?>.toId(idColumn: Column<ID>): Binder.BindingBuilder<BEAN, ID?> =
    withConverter(EntityToIdConverter(idColumn, ENTITY::class))
