package com.github.mvysny.ktormvaadin.filter

import com.github.mvysny.kaributesting.v10.*
import com.github.mvysny.ktormvaadin.utils.PopupButton
import com.vaadin.flow.component.UI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Locale
import kotlin.test.expect
import kotlin.test.fail

class DateRangePopupTest {
    @BeforeEach fun setup() { MockVaadin.setup() }
    @AfterEach fun teardown() { MockVaadin.tearDown() }
    private lateinit var component: DateRangePopup
    @BeforeEach fun createComponent() { component = DateRangePopup() }

    @Test fun `Initial value is universal`() {
        expect(DateInterval.UNIVERSAL) { component._value }
    }

    @Test fun `setting the value preserves the value`() {
        component._value = DateInterval(LocalDate.now(), LocalDate.now().plusDays(1))
        expect(DateInterval(LocalDate.now(), LocalDate.now().plusDays(1))) { component._value!! }
    }

    @Nested inner class `value change listener tests` {
        @Test fun `Setting to the default value does nothing`() {
            component.addValueChangeListener {
                fail("should not be fired")
            }
            component.value = null
        }

        @Test fun `Setting to the null value does nothing`() {
            component.addValueChangeListener {
                fail("should not be fired")
            }
            component.value = DateInterval.UNIVERSAL
        }

        @Test fun `Setting the value programatically triggers value change listeners`() {
            lateinit var newValue: DateInterval
            component.addValueChangeListener {
                expect(false) { it.isFromClient }
                expect(DateInterval.UNIVERSAL) { it.oldValue }
                newValue = it.value
            }
            component._setValue(DateInterval(LocalDate.now(), LocalDate.now().plusDays(1)), false)
            expect(DateInterval(LocalDate.now(), LocalDate.now().plusDays(1))) { newValue }
        }

        @Test fun `value change won't trigger unregistered change listeners`() {
            component.addValueChangeListener {
                fail("should not be fired")
            } .remove()
            component._value = DateInterval(LocalDate.now(), LocalDate.now().plusDays(1))
        }
    }

    /**
     * The button caption is the only thing the user sees when the popup is closed.
     */
    @Nested inner class CaptionTests {
        private val jan2 = LocalDate.of(2024, 1, 2)
        private val jan16 = LocalDate.of(2024, 1, 16)
        private fun captionOf(popup: DateRangePopup): String = popup._get<PopupButton>().caption

        @Test fun `universal set shows All`() {
            expect("All") { captionOf(component) }
            component._value = DateInterval(jan2, jan16)
            component._value = DateInterval.UNIVERSAL
            expect("All") { captionOf(component) }
        }

        @Test fun `bound interval`() {
            UI.getCurrent().locale = Locale.US
            component._value = DateInterval(jan2, jan16)
            expect("1/2/24 - 1/16/24") { captionOf(component) }
        }

        @Test fun `unbound side is left empty`() {
            UI.getCurrent().locale = Locale.US
            component._value = DateInterval(jan2, null)
            expect("1/2/24 - ") { captionOf(component) }
            component._value = DateInterval(null, jan16)
            expect(" - 1/16/24") { captionOf(component) }
        }

        /**
         * The date format follows the UI locale: the caption may end up in a Grid header
         * of a localized app.
         */
        @Test fun `dates are formatted in the UI locale`() {
            UI.getCurrent().locale = Locale.US
            component._value = DateInterval(jan2, jan16)
            val us = captionOf(component)

            UI.getCurrent().locale = Locale.GERMANY
            val germanPopup = DateRangePopup()
            germanPopup._value = DateInterval(jan2, jan16)
            val german = captionOf(germanPopup)

            expect(true, "expected different formatting, got '$us' and '$german'") { us != german }
        }
    }

    @Test fun `read-only propagates to the fields in the popup`() {
        expect(false) { component.fromField.isReadOnly }
        component.isReadOnly = true
        expect(true) { component.fromField.isReadOnly }
        expect(true) { component.toField.isReadOnly }
        component.isReadOnly = false
        expect(false) { component.fromField.isReadOnly }
        expect(false) { component.toField.isReadOnly }
    }

    @Nested inner class PopupTests {
        @Test fun `setting the value while the dialog is opened propagates the values to date fields`() {
            component._value = DateInterval(LocalDate.now(), LocalDate.now().plusDays(1))
            expect(LocalDate.now()) { component.fromField._value }
            expect(LocalDate.now().plusDays(1)) { component.toField._value }
        }

        @Test fun `Set properly sets the value to null if nothing is filled in`() {
            component._value = DateInterval(LocalDate.now(), LocalDate.now().plusDays(1))
            var wasCalled = false
            component.addValueChangeListener {
                expect(true) { it.isFromClient }
                wasCalled = true
            }
            component.fromField._value = null
            component.toField._value = null
            expect(true) { wasCalled }
            expect(DateInterval.UNIVERSAL) { component._value }
        }

        @Test fun `Set properly sets the value in`() {
            var wasCalled = false
            component.addValueChangeListener {
                expect(true) { it.isFromClient }
                wasCalled = true
            }
            component.fromField._value = LocalDate.now().minusDays(10)
            component.toField._value = LocalDate.now().plusDays(10)
            expect(true) { wasCalled }
            expect(DateInterval(LocalDate.now().minusDays(10), LocalDate.now().plusDays(10))) { component._value }
        }
    }
}
