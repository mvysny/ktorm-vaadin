package com.github.mvysny.ktormvaadin.filter


import com.github.mvysny.ktormvaadin.utils.PopupButton
import com.vaadin.flow.component.HasValue.ValueChangeListener
import com.vaadin.flow.component.customfield.CustomField
import com.vaadin.flow.component.customfield.CustomFieldVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.menubar.MenuBarVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.TextFieldVariant

/**
 * Only shows a single button as its contents. When the button is clicked, it opens a dialog and allows the user to specify a range
 * of numbers. When the user sets the values, the dialog is
 * hidden and the number range is set as the value of the popup.
 *
 * The current numeric range is also displayed as the caption of the button.
 *
 * [getValue] never returns null - by default an [universal set][ClosedInterval.isUniversalSet]
 * is returned. Use [NumberInterval.contains] to easily convert this filter to a SQL statement.
 */
class NumberRangePopup(id: String = "") : CustomField<NumberInterval<Double>>(NumberInterval<Double>(null, null)) {
    /**
     * The "From" field, shown in the popup. You can customize the field to e.g.
     * set a localized caption, or add themes/CSS classes.
     */
    val fromField: NumberField = NumberField("", "From")
    /**
     * The "To" field, shown in the popup. You can customize the field to e.g.
     * set a localized caption, or add themes/CSS classes.
     */
    val toField: NumberField = NumberField("", "To")

    /**
     * The popup button, shown by this [CustomField].
     */
    private val content = PopupButton()

    /**
     * Creates the popup.
     */
    init {
        setId(id)
        fromField.setId("from")
        toField.setId("to")
        fromField.addThemeVariants(TextFieldVariant.LUMO_SMALL)
        fromField.setClearButtonVisible(true)
        toField.addThemeVariants(TextFieldVariant.LUMO_SMALL)
        toField.setClearButtonVisible(true)
        content.setPopupContent(
            HorizontalLayout(
                fromField,
                Span(".."),
                toField
            )
        )
        fromField.addValueChangeListener(ValueChangeListener { e: ComponentValueChangeEvent<NumberField?, Double?>? ->
            updateValue()
            updateCaption()
        })
        toField.addValueChangeListener(ValueChangeListener { e: ComponentValueChangeEvent<NumberField?, Double?>? ->
            updateValue()
            updateCaption()
        })
        updateCaption()
        add(content)
        content.addThemeVariants(MenuBarVariant.LUMO_SMALL)
        addThemeVariants(CustomFieldVariant.LUMO_SMALL)
    }

    override fun generateModelValue(): NumberInterval<Double> {
        return NumberInterval<Double>(fromField.getValue(), toField.getValue())
    }

    override fun setValue(value: NumberInterval<Double>?) {
        var value = value
        if (value == null) {
            value = NumberInterval<Double>(null, null)
        }
        super.setValue(value)
    }

    override fun setPresentationValue(newPresentationValue: NumberInterval<Double>?) {
        var newPresentationValue = newPresentationValue
        if (newPresentationValue == null) {
            newPresentationValue = NumberInterval<Double>(null, null)
        }
        fromField.setValue(newPresentationValue.start)
        toField.setValue(newPresentationValue.endInclusive)
        updateCaption()
    }

    private fun updateCaption() {
        val value = getValue()
        if (value == null || value.isUniversalSet) {
            content.caption = "All"
        } else if (value.isSingleItem) {
            content.caption = "[x] = " + value.start
        } else if (value.isBound) {
            content.caption =
                value.start.toString() + " ≤ [x] ≤ " + value.endInclusive
        } else if (value.start != null) {
            content.caption = "[x] ≥ " + value.start
        } else {
            content.caption = "[x] ≤ " + value.endInclusive
        }
    }

    override fun setReadOnly(readOnly: Boolean) {
        super.setReadOnly(readOnly)
        fromField.isReadOnly = readOnly
        toField.isReadOnly = readOnly
    }
}
