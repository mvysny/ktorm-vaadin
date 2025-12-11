package com.github.mvysny.ktormvaadin.filter

import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.combobox.ComboBoxVariant

/**
 * Allows the user to select 'true'/'false'/null.
 * If the value is non-null, the filtering mechanism should apply appropriate filter;
 * when null, no filter should be applied.
 */
class BooleanFilterField : ComboBox<Boolean?>() {
    init {
        isClearButtonVisible = true
        setItems(true, false)
        addThemeVariants(ComboBoxVariant.LUMO_SMALL)
    }
}
