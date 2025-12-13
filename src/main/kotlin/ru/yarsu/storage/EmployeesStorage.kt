package ru.yarsu.storage

import ru.yarsu.data.Employees
import ru.yarsu.jwt.JwtTools
import java.time.LocalDate
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

    fun getEmployeesByPosition(position: String): List<Employees> = employeesList.filter { it.position == position }

    fun getEmployeesByName(name: String): List<Employees> = employeesList.filter { it.name.contains(name) }

    fun getEmployeeByEmail(email: String): Employees? = employeesList.find { it.email == email }

    fun getEmployeeByDate(date: LocalDate): List<Employees> = employeesList.filter { it.registrationDateTime.toLocalDate() == date }

    fun deleteEmployee(id: UUID): Boolean = employeesList.removeIf { it.id == id }
}
