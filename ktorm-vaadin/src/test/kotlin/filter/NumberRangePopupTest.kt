package com.github.mvysny.ktormvaadin.filter

import com.github.mvysny.kaributesting.v10.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.expect
import kotlin.test.fail

class NumberRangePopupTest {
    private lateinit var component: NumberRangePopup
    @BeforeEach fun setup() {
        MockVaadin.setup()
        component = NumberRangePopup()
    }
    @AfterEach fun teardown() { MockVaadin.tearDown() }

    @Test fun `Initial value is universal set`() {
        expect(true) { component._value!!.isUniversalSet }
    }

    @Test fun `setting the value preserves the value`() {
        component._value = NumberInterval(5.0, 25.0)
        expect(NumberInterval(5.0, 25.0)) { component._value!! }
    }

    @Nested inner class `value change listener tests` {
        @Test fun `Setting to the default value doesn't fire the listener`() {
            component.addValueChangeListener {
                fail("should not be fired")
            }
            component.value = NumberInterval(null, null)
        }

        @Test fun `Setting to the null value doesn't fire the listener`() {
            component.addValueChangeListener {
                fail("should not be fired")
            }
            component.value = null
        }

        @Test fun `Setting the value programatically triggers value change listeners`() {
            lateinit var newValue: NumberInterval<Double>
            component.addValueChangeListener {
                expect(false) { it.isFromClient }
                expect(true) { it.oldValue!!.isUniversalSet }
                newValue = it.value
            }
            component._setValue(NumberInterval(5.0, 25.0), false)
            expect(NumberInterval(5.0, 25.0)) { newValue }
        }

        @Test fun `value change won't trigger unregistered change listeners`() {
            component.addValueChangeListener {
                fail("should not be fired")
            } .remove()
            component._value = NumberInterval(5.0, 25.0)
        }
    }

    @Nested inner class PopupTests {
        @Test fun `setting the value while the dialog is opened propagates the values to date fields`() {
            component._value = NumberInterval(5.0, 25.0)
            expect(5.0) { component.fromField._value }
            expect(25.0) { component.toField._value }
        }

        @Test fun `Set properly sets the value to universal set if nothing is filled in`() {
            component._value = NumberInterval(25.0, 35.0)
            var wasCalled = false
            component.addValueChangeListener {
                expect(true) { it.isFromClient }
                wasCalled = true
            }
            component.fromField._value = null
            component.toField._value = null
            expect(true) { wasCalled }
            expect(true) { component._value!!.isUniversalSet }
        }

        @Test fun `Set properly sets the value in`() {
            var wasCalled = false
            component.addValueChangeListener {
                expect(true) { it.isFromClient }
                wasCalled = true
            }
            component.fromField._value = 25.0
            component.toField._value = 35.0
            expect(true) { wasCalled }
            expect(NumberInterval(25.0, 35.0)) { component._value }
        }
    }
}
