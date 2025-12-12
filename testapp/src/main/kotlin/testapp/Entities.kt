package testapp

import com.github.mvysny.ktormvaadin.ActiveEntity
import com.github.mvysny.ktormvaadin.db
import com.github.mvysny.ktormvaadin.deleteAll
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.ktorm.dsl.update
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalDate

// See https://www.ktorm.org/en/schema-definition.html
object Departments : Table<Department>("t_department") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("NAME").bindTo { it.name }
    val location = varchar("location").bindTo { it.location }
}

object Employees : Table<Employee>("t_employee") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("NAME").bindTo { it.name }
    val job = varchar("job").bindTo { it.job }
    val managerId = int("manager_id").bindTo { it.managerId }
    val hireDate = date("hire_date").bindTo { it.hireDate }
    val salary = long("salary").bindTo { it.salary }
    val departmentId = int("department_id").bindTo { it.departmentId }
}

interface Department : ActiveEntity<Department> {
    val id: Int

    @get:NotNull
    @get:NotBlank
    @get:Size(min = 1, max = 255)
    var name: String

    @get:NotNull
    @get:NotBlank
    @get:Size(min = 1, max = 255)
    var location: String
    override val table: Table<Department> get() = Departments

    companion object : Entity.Factory<Department>()
}

interface Employee : ActiveEntity<Employee> {
    val id: Int

    @get:NotNull
    @get:NotBlank
    @get:Size(min = 1, max = 255)
    var name: String

    @get:NotNull
    @get:NotBlank
    @get:Size(min = 1, max = 255)
    var job: String
    var managerId: Int?

    @get:NotNull
    var hireDate: LocalDate?

    @get:NotNull
    var salary: Long

    @get:NotNull
    var departmentId: Int
    override val table: Table<Employee> get() = Employees

    companion object : Entity.Factory<Employee>()
}

fun demoData() {
    db {
        database.update(Employees) {
            set(it.managerId, null)
        }
        Employees.deleteAll()
        Departments.deleteAll()

        val departments = (0..100).map {
            Department { name = "Dept $it"; location = "Somewhere $it" }.apply { create() }
        }
        val managers = (0..10).map {
            Employee {
                name = "Manager $it"; job = "Manager"; hireDate = LocalDate.of(2025, 11, 12)
                salary = 6000 + it.toLong(); departmentId = departments.random().id
            }.apply { create() }
        }
        (0..100).map {
            Employee {
                name = "Employee $it"; job = "Employee"; hireDate = LocalDate.of(2025, 11, 12)
                salary = 6000 + it.toLong(); departmentId = departments.random().id
            }.apply { create() }
        }
    }
}