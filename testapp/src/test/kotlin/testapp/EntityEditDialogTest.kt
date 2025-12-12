package testapp

import com.github.mvysny.ktormvaadin.findAll
import org.junit.jupiter.api.Test

class EntityEditDialogTest : AbstractAppTest() {
    @Test fun smoke() {
        EntityEditDialog<Department>(Department{}, "entiity", DepartmentForm()) {} .open()
        val dept = Departments.findAll()[0]
        EntityEditDialog<Department>(dept, "entiity", DepartmentForm()) {} .open()
    }
}