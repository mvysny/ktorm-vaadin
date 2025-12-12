package com.github.mvysny.ktormvaadin

import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.BeanValidationBinder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
    }
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