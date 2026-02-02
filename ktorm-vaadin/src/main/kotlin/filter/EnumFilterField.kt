package com.github.mvysny.ktormvaadin.filter

import com.vaadin.flow.component.combobox.MultiSelectComboBox
import com.vaadin.flow.component.combobox.MultiSelectComboBoxVariant


/**
 * Allows the user to pick a bunch of enum constants. If no constants are picked, the filter
 * should be ignored. Use [.isAllOrNothingSelected] to detect this.
 *
 * If the field should allow only a subset of enum constants to be selected, call [.setItems]
 * to set allowed enum constants.
 *
 * No enum constants are selected by default.
 * @param <E> the type of enum constants shown in this field.
 */
class EnumFilterField<E : Enum<E>>(enumClass: Class<E>, id: String = "") : MultiSelectComboBox<E>() {
    /**
     * Creates the field.
     * @param enumClass the enum class, not null.
     */
    init {
        setId(id)
        isClearButtonVisible = true
        addThemeVariants(MultiSelectComboBoxVariant.LUMO_SMALL)
        setItems(*enumClass.getEnumConstants())
    }

    val isAllSelected: Boolean
        /**
         * Returns true if all enum constants are selected.
         * @return true if all enum constants are selected.
         */
        get() = selectedItems.size == listDataView.itemCount

    val isAllOrNothingSelected: Boolean
        /**
         * Returns true if either all constants are selected, or none are selected.
         * If this returns false, this filter should be ignored and not applied to the SQL query.
         * @return false if this filter should be ignored since it matches all enum constants.
         */
        get() = selectedItems.isEmpty() || this.isAllSelected
}
