package com.github.mvysny.ktormvaadin.filter


import com.github.mvysny.ktormvaadin.utils.PopupButton
import com.vaadin.flow.component.HasValue.ValueChangeListener
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.customfield.CustomField
import com.vaadin.flow.component.customfield.CustomFieldVariant
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.datepicker.DatePickerVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.menubar.MenuBarVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

/**
 * Only shows a single button as its contents. When the button is clicked, it opens a dialog and allows the user to specify a range
 * of dates. When the user sets the values, the dialog is
 * hidden and the number range is set as the value of the popup.
 *
 * The current date range is also displayed as the caption of the button.
 *
 * [value] never returns null - by default an [universal set][ClosedInterval.isUniversalSet]
 * is returned.
 */
class DateRangePopup : CustomField<DateInterval>(DateInterval.UNIVERSAL) {
    /**
     * The "From" field, shown in the popup. You can customize the field to e.g.
     * set a localized caption, or add themes/CSS classes.
     */
    val fromField: DatePicker = DatePicker()
    /**
     * The "To" field, shown in the popup. You can customize the field to e.g.
     * set a localized caption, or add themes/CSS classes.
     */
    val toField: DatePicker = DatePicker()

    /**
     * The popup button, shown by this [CustomField].
     */
    private val content = PopupButton()

    /**
     * Creates the popup.
     */
    init {
        fromField.setId("from")
        fromField.placeholder = "From"
        toField.setId("to")
        toField.placeholder = "To"
        fromField.addThemeVariants(DatePickerVariant.LUMO_SMALL)
        fromField.isClearButtonVisible = true
        toField.addThemeVariants(DatePickerVariant.LUMO_SMALL)
        toField.isClearButtonVisible = true
        content.setPopupContent(
            HorizontalLayout(
                fromField,
                Span(".."),
                toField
            )
        )
        fromField.addValueChangeListener(ValueChangeListener {
            updateValue()
            updateCaption()
        })
        toField.addValueChangeListener(ValueChangeListener {
            updateValue()
            updateCaption()
        })
        updateCaption()
        add(content)
        content.addThemeVariants(MenuBarVariant.LUMO_SMALL)
        addThemeVariants(CustomFieldVariant.LUMO_SMALL)
    }

    override fun generateModelValue(): DateInterval =
        DateInterval(fromField.value, toField.value)

    override fun setValue(value: DateInterval?) {
        var value = value
        if (value == null) {
            value = DateInterval.UNIVERSAL
        }
        super.setValue(value)
    }

    override fun setPresentationValue(newPresentationValue: DateInterval?) {
        var newPresentationValue = newPresentationValue
        if (newPresentationValue == null) {
            newPresentationValue = DateInterval.UNIVERSAL
        }
        fromField.value = newPresentationValue.start
        toField.value = newPresentationValue.endInclusive
        updateCaption()
    }

    private fun updateCaption() {
        val value = getValue()
        if (value == null || value.isUniversalSet) {
            content.caption = "All"
        } else {
            content.caption =
                format(value.start) + " - " + format(value.endInclusive)
        }
    }

    private val formatter: DateTimeFormatter
        get() {
            val locale = UI.getCurrent().getLocale()
            // use SHORT formatting: if the component is present in grid header cell,
            // the text "Jan 2, 2024 - Jan 16, 2024" may be twice as long as the cell
            // content (date formatted as "Jan 2, 2024").
            return DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(if (locale == null) Locale.getDefault() else locale)
        }

    private fun format(date: LocalDate?): String {
        return if (date == null) "" else this.formatter.format(date)
    }

    override fun setReadOnly(readOnly: Boolean) {
        super.setReadOnly(readOnly)
        fromField.isReadOnly = readOnly
        toField.isReadOnly = readOnly
    }
}
