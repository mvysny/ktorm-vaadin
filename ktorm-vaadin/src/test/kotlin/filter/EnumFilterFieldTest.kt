package com.github.mvysny.ktormvaadin.filter

import com.github.mvysny.kaributesting.v10.MockVaadin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.expect

enum class MaritalStatus {
    married,
    divorced,
    single
}

class EnumFilterFieldTest {
    @BeforeEach fun setup() { MockVaadin.setup() }
    @AfterEach fun teardown() { MockVaadin.tearDown() }

    private val f = EnumFilterField(MaritalStatus::class.java)

    @Test
    fun smoke() {
        expect(true) { f.isAllOrNothingSelected }
    }

    @Test
    fun nothingIsSelectedByDefault() {
        expect(setOf()) { f.selectedItems }
        expect(false) { f.isAllSelected }
        expect(true) { f.isAllOrNothingSelected }
    }

    /**
     * A filter matching every constant filters nothing out, which is why
     * [EnumFilterField.isAllOrNothingSelected] lumps it together with the empty selection.
     */
    @Test
    fun selectingAllConstantsIsAllOrNothing() {
        f.select(*MaritalStatus.entries.toTypedArray())
        expect(true) { f.isAllSelected }
        expect(true) { f.isAllOrNothingSelected }
    }

    @Test
    fun selectingSomeConstantsMustBeFiltered() {
        f.select(MaritalStatus.married)
        expect(false) { f.isAllSelected }
        expect(false) { f.isAllOrNothingSelected }
    }

    /**
     * The field may be limited to a subset of constants; [EnumFilterField.isAllSelected]
     * then refers to that subset.
     */
    @Test
    fun setItemsLimitsTheConstantsOffered() {
        f.setItems(MaritalStatus.married, MaritalStatus.divorced)
        f.select(MaritalStatus.married, MaritalStatus.divorced)
        expect(true) { f.isAllSelected }
    }
}
