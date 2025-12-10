package testapp

import com.github.mvysny.ktormvaadin.ActiveEntity
import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import java.time.LocalDate

// See https://www.ktorm.org/en/schema-definition.html
object Departments : Table<Department>("t_department") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val location = varchar("location").bindTo { it.location }
}

object Employees : Table<Employee>("t_employee") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
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
}