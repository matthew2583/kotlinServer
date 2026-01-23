package ru.yarsu.storage

import ru.yarsu.auth.JwtTools
import ru.yarsu.data.Employees
import java.time.LocalDateTime
import java.util.UUID

class EmployeesStorage(
    initialEmployees: List<Employees> = emptyList(),
    val secret: String,
) {
    private val employeesList = initialEmployees.toMutableList()
    private val tokens: MutableList<String> = mutableListOf()

    init {
        employeesList.forEach { employee ->
            createToken(employee.id)
        }
    }

    fun createToken(id: UUID) {
        val now = LocalDateTime.now()
        tokens.add(JwtTools(secret).createJwt(sub = id, exp = now))
    }

    fun getTokens(): List<String> = tokens

    fun getAllEmployees(): List<Employees> = employeesList.toList()

    fun getEmployeesById(id: UUID?): Employees? = employeesList.find { it.id == id }
}
