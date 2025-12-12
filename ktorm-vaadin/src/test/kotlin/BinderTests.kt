package com.github.mvysny.ktormvaadin

import com.vaadin.flow.component.textfield.IntegerField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.BeanValidationBinder
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

    @Test
    fun valuePropagatedFromBeanToForm() {
        val form = PersonForm()
        val person = Person { name = "Rimmer"; age = 35 }
        form.binder.readBean(person)
        expect("Rimmer") { form.nameField.value}
        expect(35) { form.ageField.value}
    }

    @Test
    fun valuesPropagatedFromFormToBean() {
       val form = PersonForm()
       form.nameField.value = "Rimmer"
       form.ageField.value = 35
       val p = Person{}
       form.binder.writeBean(p)
       expect("Rimmer") { p.name }
       expect(35) { p.age }
    }

    @Test
    fun validationWorks() {
        // test for https://hibernate.atlassian.net/browse/HV-2018
        val form = PersonForm()
        expect(false) { form.binder.writeBeanIfValid(Person{}) }
    }
}