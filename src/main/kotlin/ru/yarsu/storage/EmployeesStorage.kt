package ru.yarsu.storage

import ru.yarsu.data.Employees
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class EmployeesStorage(
    initialEmployees: List<Employees> = emptyList(),
) {
    private val employeesList = initialEmployees.toMutableList()

    fun getAllEmployees(): List<Employees> = employeesList.toList()

    fun getEmployeesById(id: UUID?): Employees? = employeesList.find { it.id == id }

    fun getEmployeesByPosition(position: String): List<Employees> = employeesList.filter { it.position == position }

    fun getEmployeesByName(name: String): List<Employees> = employeesList.filter { it.name.contains(name) }

    fun getEmployeeByEmail(email: String): Employees? = employeesList.find { it.email == email }

    fun getEmployeeByDate(date: LocalDate): List<Employees> = employeesList.filter { it.registrationDateTime.toLocalDate() == date }

    fun addEmployee(
        name: String,
        position: String,
        email: String,
    ): UUID {
        val newEmployee =
            Employees(
                id = UUID.randomUUID(),
                name = name,
                position = position,
                registrationDateTime = LocalDateTime.now(),
                email = email,
            )
        employeesList.add(newEmployee)
        return newEmployee.id
    }

    fun updateEmployee(
        id: UUID,
        name: String,
        position: String,
        email: String,
    ): Boolean {
        val index = employeesList.indexOfFirst { it.id == id }
        if (index == -1) return false

        val updatedEmployee =
            Employees(
                id = id,
                name = name,
                position = position,
                registrationDateTime = employeesList[index].registrationDateTime,
                email = email,
            )

        employeesList[index] = updatedEmployee
        return true
    }

    fun deleteEmployee(id: UUID): Boolean = employeesList.removeIf { it.id == id }
}
