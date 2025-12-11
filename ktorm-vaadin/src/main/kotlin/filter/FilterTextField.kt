package com.github.mvysny.ktormvaadin.filter

import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.textfield.TextFieldVariant
import com.vaadin.flow.data.value.ValueChangeMode


/**
 * A text field that's modified for the use in the filter bar: it's smaller,
 * shows the clear button and fires value change events faster than the default text field
 * (which fires on blur).
 */
class FilterTextField(id: String) : TextField() {
    init {
        setId(id)
        addThemeVariants(TextFieldVariant.LUMO_SMALL)
        isClearButtonVisible = true
        valueChangeMode = ValueChangeMode.LAZY
    }
}