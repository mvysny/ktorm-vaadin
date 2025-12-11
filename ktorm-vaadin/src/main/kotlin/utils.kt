package com.github.mvysny.ktormvaadin

import org.ktorm.dsl.and
import org.ktorm.schema.ColumnDeclaring

@JvmName("and_nulls")
fun Collection<ColumnDeclaring<Boolean>?>.and(): ColumnDeclaring<Boolean>? = filterNotNull().and()
fun Collection<ColumnDeclaring<Boolean>>.and(): ColumnDeclaring<Boolean>? = when {
    isEmpty() -> null
    size == 1 -> single()
    else -> reduce { a, b -> a and b}
}