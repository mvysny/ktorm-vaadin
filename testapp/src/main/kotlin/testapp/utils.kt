package testapp

import org.ktorm.dsl.and
import org.ktorm.schema.ColumnDeclaring

fun Collection<ColumnDeclaring<Boolean>>.and(): ColumnDeclaring<Boolean>? = when {
    isEmpty() -> null
    size == 1 -> single()
    else -> reduce { a, b -> a and  b}
}