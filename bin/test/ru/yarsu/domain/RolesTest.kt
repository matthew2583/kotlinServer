package ru.yarsu.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class RolesTest {
    @Test
    fun employeeHasCorrectDescription() {
        assertEquals("Зарегистрированный работник", Roles.Employee.description)
    }

    @Test
    fun managerHasCorrectDescription() {
        assertEquals("Менеджер", Roles.Manager.description)
    }

    @Test
    fun userManagerHasCorrectDescription() {
        assertEquals("Менеджер пользователей приложения", Roles.UserManager.description)
    }

    @ParameterizedTest
    @CsvSource(
        "Employee, Employee",
        "Manager, Manager",
        "UserManager, UserManager",
    )
    fun fromStringParsesValidRoleName(
        roleName: String,
        expectedEnumName: String,
    ) {
        val result = Roles.fromString(roleName)
        assertEquals(Roles.valueOf(expectedEnumName), result)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "employee", "EMPLOYEE", "admin", "user", "invalid", "Менеджер"])
    fun fromStringThrowsExceptionForInvalidValue(invalidValue: String) {
        val exception =
            assertThrows<IllegalArgumentException> {
                Roles.fromString(invalidValue)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("Недопустимая роль"))
    }

    @Test
    fun rolesHasExactly3Entries() {
        assertEquals(3, Roles.entries.size)
    }

    @Test
    fun allRolesHaveNonEmptyDescription() {
        Roles.entries.forEach { role ->
            assertTrue(role.description.isNotBlank(), "Description for ${role.name} should not be blank")
        }
    }

    @Test
    fun allRolesHaveUniqueName() {
        val names = Roles.entries.map { it.name }
        assertEquals(names.size, names.distinct().size, "All role names should be unique")
    }
}
