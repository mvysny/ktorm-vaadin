package testapp

import com.github.mvysny.ktormvaadin.ActiveEntity
import com.github.mvysny.ktormvaadin.db
import com.github.mvysny.ktormvaadin.deleteAll
import org.ktorm.dsl.update
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalDate
import kotlin.random.Random

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
    val managerId = int("manager_id").bindTo { it.manager?.id }
    val hireDate = date("hire_date").bindTo { it.hireDate }
    val salary = long("salary").bindTo { it.salary }
    val departmentId = int("department_id").references(Departments) { it.department }
}

interface Department : ActiveEntity<Department> {
    val id: Int
    var name: String
    var location: String
    override val table: Table<Department> get() = Departments
    companion object : Entity.Factory<Department>()
}

interface Employee : ActiveEntity<Employee> {
    val id: Int
    var name: String
    var job: String
    var manager: Employee?
    var hireDate: LocalDate
    var salary: Long
    var department: Department
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
            Department{name = "Dept $it"; location = "Somewhere $it"}.apply { create() }
        }
        val managers = (0..10).map {
           Employee{name = "Manager $it"; job = "Manager"; hireDate = LocalDate.now(); salary =
               Random.nextLong(1000, 10000); department = departments.random()
           }.apply { create() }
        }
        (0..100).map {
            Employee{name = "Employee $it"; job = "Employee"; hireDate = LocalDate.now();
            salary = Random.nextLong(1000, 10000); department = departments.random()}.apply { create() }
        }
    }
}