package testapp

import org.junit.jupiter.api.Test

class EntityEditDialogTest : AbstractAppTest() {
    @Test fun smoke() {
        EntityEditDialog<Department>(Department{}, "entiity", DepartmentForm()) {} .open()
    }
}