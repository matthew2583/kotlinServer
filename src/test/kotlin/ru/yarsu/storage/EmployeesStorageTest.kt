package ru.yarsu.storage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import ru.yarsu.data.Employees
import ru.yarsu.domain.Roles
import java.time.LocalDateTime
import java.util.UUID

class EmployeesStorageTest {
    private lateinit var storage: EmployeesStorage
    private val testSecret = "test-secret"

    @BeforeEach
    fun setUp() {
        storage = EmployeesStorage(secret = testSecret)
    }

    @Nested
    inner class GetEmployeeTests {
        @Test
        fun getEmployeesByIdReturnsNullForNonExistent() {
            val employee = storage.getEmployeesById(UUID.randomUUID())
            assertNull(employee)
        }

        @Test
        fun getAllEmployeesReturnsEmptyListInitially() {
            val employees = storage.getAllEmployees()
            assertTrue(employees.isEmpty())
        }
    }

    @Nested
    inner class TokenTests {
        @Test
        fun tokensListIsEmptyInitially() {
            val tokens = storage.getTokens()
            assertTrue(tokens.isEmpty())
        }

        @Test
        fun createTokenAddsToList() {
            storage.createToken(UUID.randomUUID())
            val tokens = storage.getTokens()
            assertEquals(1, tokens.size)
        }
    }

    @Nested
    inner class InitWithListTests {
        @Test
        fun storageCanBeInitializedWithList() {
            val employee =
                Employees(
                    id = UUID.randomUUID(),
                    name = "Kozlov",
                    position = "Administrator",
                    registrationDateTime = LocalDateTime.now(),
                    email = "kozlov@company.ru",
                    role = Roles.UserManager,
                )

            val storageWithData =
                EmployeesStorage(
                    initialEmployees = listOf(employee),
                    secret = testSecret,
                )

            assertEquals(1, storageWithData.getAllEmployees().size)
            assertNotNull(storageWithData.getEmployeesById(employee.id))
        }

        @Test
        fun tokensCreatedForInitialEmployees() {
            val employee =
                Employees(
                    id = UUID.randomUUID(),
                    name = "Test",
                    position = "Test",
                    registrationDateTime = LocalDateTime.now(),
                    email = "test@company.ru",
                    role = Roles.Employee,
                )

            val storageWithData =
                EmployeesStorage(
                    initialEmployees = listOf(employee),
                    secret = testSecret,
                )

            assertEquals(1, storageWithData.getTokens().size)
        }
    }

    @Nested
    inner class AllRolesTests {
        @Test
        fun storageAcceptsAllRoles() {
            Roles.entries.forEach { role ->
                val employee =
                    Employees(
                        id = UUID.randomUUID(),
                        name = "Test ${role.name}",
                        position = "Position",
                        registrationDateTime = LocalDateTime.now(),
                        email = "${role.name.lowercase()}@company.ru",
                        role = role,
                    )

                val roleStorage =
                    EmployeesStorage(
                        initialEmployees = listOf(employee),
                        secret = testSecret,
                    )

                assertEquals(1, roleStorage.getAllEmployees().size)
                assertEquals(role, roleStorage.getAllEmployees().first().role)
            }
        }
    }
}
