package ru.yarsu.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import ru.yarsu.domain.Roles
import java.time.LocalDateTime
import java.util.UUID

class EmployeesTest {
    @Test
    fun employeeCreatedWithAllProperties() {
        val id = UUID.randomUUID()
        val regDateTime = LocalDateTime.of(2024, 1, 15, 9, 0)

        val employee =
            Employees(
                id = id,
                name = "Иванов Иван Иванович",
                position = "Менеджер по продажам",
                registrationDateTime = regDateTime,
                email = "ivanov@company.ru",
                role = Roles.Manager,
            )

        assertEquals(id, employee.id)
        assertEquals("Иванов Иван Иванович", employee.name)
        assertEquals("Менеджер по продажам", employee.position)
        assertEquals(regDateTime, employee.registrationDateTime)
        assertEquals("ivanov@company.ru", employee.email)
        assertEquals(Roles.Manager, employee.role)
    }

    @Test
    fun dataClassImplementsEqualsCorrectly() {
        val id = UUID.randomUUID()
        val regDateTime = LocalDateTime.of(2024, 1, 15, 9, 0)

        val employee1 =
            Employees(
                id = id,
                name = "Петров Пётр",
                position = "Работник",
                registrationDateTime = regDateTime,
                email = "petrov@company.ru",
                role = Roles.Employee,
            )

        val employee2 =
            Employees(
                id = id,
                name = "Петров Пётр",
                position = "Работник",
                registrationDateTime = regDateTime,
                email = "petrov@company.ru",
                role = Roles.Employee,
            )

        assertEquals(employee1, employee2)
        assertEquals(employee1.hashCode(), employee2.hashCode())
    }

    @Test
    fun employeesWithDifferentIdsAreNotEqual() {
        val regDateTime = LocalDateTime.now()

        val employee1 = createEmployee(UUID.randomUUID(), regDateTime)
        val employee2 = createEmployee(UUID.randomUUID(), regDateTime)

        assertNotEquals(employee1, employee2)
    }

    @Test
    fun copyCreatesModifiedCopy() {
        val employee = createEmployee(UUID.randomUUID(), LocalDateTime.now())

        val promoted = employee.copy(position = "Инженер", role = Roles.Manager)

        assertEquals("Инженер", promoted.position)
        assertEquals(Roles.Manager, promoted.role)
        assertEquals(employee.id, promoted.id)
        assertEquals(employee.name, promoted.name)
    }

    @Test
    fun supportsAllRoles() {
        Roles.entries.forEach { role ->
            val employee =
                Employees(
                    id = UUID.randomUUID(),
                    name = "Test",
                    position = "Test Position",
                    registrationDateTime = LocalDateTime.now(),
                    email = "test@company.ru",
                    role = role,
                )
            assertEquals(role, employee.role)
        }
    }

    @Test
    fun supportsCyrillicNames() {
        val employee =
            Employees(
                id = UUID.randomUUID(),
                name = "Козлов Андрей Петрович",
                position = "Начальник смены",
                registrationDateTime = LocalDateTime.now(),
                email = "kozlov@company.ru",
                role = Roles.Manager,
            )

        assertEquals("Козлов Андрей Петрович", employee.name)
        assertEquals("Начальник смены", employee.position)
    }

    private fun createEmployee(
        id: UUID,
        regDateTime: LocalDateTime,
    ): Employees =
        Employees(
            id = id,
            name = "Сидоров",
            position = "Инженер",
            registrationDateTime = regDateTime,
            email = "sidorov@company.ru",
            role = Roles.Employee,
        )
}
