package com.github.mvysny.ktormvaadin

import com.github.mvysny.kaributesting.v10._value
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.BeanValidationBinder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import kotlin.test.expect

class BinderTests {
    /**
     * Uses [bind] to bind fields to entity properties.
     */
    class PersonForm {
        val nameField = TextField()
        val ageField = IntegerField()
        val binder = BeanValidationBinder(Person::class.java)

        init {
            binder.forField(nameField).bind(Persons.name)
            binder.forField(ageField).bind(Persons.age)
        }
    }
    @Nested inner class bindTests {
        @Test
        fun valuePropagatedFromBeanToForm() {
            val form = PersonForm()
            val person = Person { name = "Rimmer"; age = 35 }
            form.binder.readBean(person)
            expect("Rimmer") { form.nameField.value }
            expect(35) { form.ageField.value }
        }

        @Test
        fun valuesPropagatedFromFormToBean() {
            val form = PersonForm()
            form.nameField.value = "Rimmer"
            form.ageField.value = 35
            val p = Person {}
            form.binder.writeBean(p)
            expect("Rimmer") { p.name }
            expect(35) { p.age }
        }
    }

    /**
     * A Kotlin property named `isActive` is named `active` in the Java bean property set
     * which [com.vaadin.flow.data.binder.Binder] uses; [bind] must perform the conversion.
     */
    @Nested inner class isPrefixedPropertyTests {
        @Test
        fun valuePropagatedFromBeanToForm() {
            val form = RobotForm()
            form.binder.readBean(Robot { isActive = true })
            expect(true) { form.activeField.value }
        }

        @Test
        fun valuePropagatedFromFormToBean() {
            val form = RobotForm()
            form.activeField.value = true
            val robot = Robot {}
            form.binder.writeBean(robot)
            expect(true) { robot.isActive }
        }
    }

    @Test
    fun validationWorks() {
        // test for https://hibernate.atlassian.net/browse/HV-2018
        val form = PersonForm()
        expect(false) { form.binder.writeBeanIfValid(Person{}) }
    }
    @Test
    fun validationWorksAddressForm() {
        // test for https://hibernate.atlassian.net/browse/HV-2018
        val form = AddressForm()
        expect(false) { form.binder.writeBeanIfValid(Address{}) }
    }
    @Nested inner class toIdTests : AbstractDbTest() {
        lateinit var person: Person

        @BeforeEach
        fun createPerson() {
            Persons.ddl()
            person = Person { name = "foo"; age = 25 }.save()
        }

        @AfterEach
        fun tearDownTestData() {
            db { ddl("drop table if exists person") }
        }
        @Test fun readBeanWithNullPersonIdResetsComboBox() {
            val form = AddressForm()
            form.personPicker.value = person
            form.binder.readBean(Address{})
            expect(null) { form.personPicker.value }
        }
        @Test fun readBeanPopulatesComboBox() {
            val form = AddressForm()
            val address = Address { of_person_id = person.id }
            form.binder.readBean(address)
            expect(person) { form.personPicker.value }
        }
        @Test fun writeBeanSetsNullPersonId() {
            val form = AddressForm()
            form.streetField._value = "foo"
            val address = Address { of_person_id = person.id }
            form.binder.writeBean(address)
            expect(null) { address.of_person_id }
        }
        @Test fun writeBeanSetsPersonId() {
            val form = AddressForm()
            form.streetField._value = "foo"
            form.personPicker._value = person
            val address = Address {}
            form.binder.writeBean(address)
            expect(person.id) { address.of_person_id }
        }
    }
}

/**
 * Minimal form for [Robot], to test the binding of a property whose name starts with "is".
 */
class RobotForm {
    val activeField = Checkbox()
    val binder = BeanValidationBinder(Robot::class.java)
    init {
        binder.forField(activeField).bind(Robots.isActive)
    }
}

/**
 * Never created in the database: the binding to [Robot] is resolved from the column
 * bindings alone.
 */
object Robots : Table<Robot>("robot") {
    val id = int("ID").primaryKey().bindTo { it.id }
    val isActive = boolean("ACTIVE").bindTo { it.isActive }
}

interface Robot : ActiveEntity<Robot> {
    val id: Int?
    var isActive: Boolean

    override val table: Table<Robot> get() = Robots
    companion object : Entity.Factory<Robot>()
}

/**
 * Minimal form, the components aren't inserted into any layout, but we only care
 * about the Binder here.
 */
class AddressForm {
    val streetField = TextField()
    val cityField = TextField()
    val personPicker = ComboBox<Person>()
    val binder = BeanValidationBinder(Address::class.java)
    init {
        personPicker.setItems(Persons.dataProvider.withStringFilterOn(Persons.name))
        binder.forField(streetField).bind(Addresses.street)
        binder.forField(cityField).bind(Addresses.city)
        binder.forField(personPicker).toId(Persons.id).bind(Addresses.of_person_id)
    }
}