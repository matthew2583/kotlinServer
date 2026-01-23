package ru.yarsu.cli.args

import com.beust.jcommander.ParameterException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KeyArgsTest {
    private val validator = SecretValidator()

    @Test
    fun regularSecretsAreAccepted() {
        val validSecrets = listOf("secret", "my-secret-key", "a", "very-long-secret-key")
        validSecrets.forEach { secret ->
            assertDoesNotThrow { validator.validate("--secret", secret) }
        }
    }

    @Test
    fun secretWithSpecialCharsIsAccepted() {
        assertDoesNotThrow { validator.validate("--secret", "secret!@#\$%^&*()") }
    }

    @Test
    fun secretWithDigitsIsAccepted() {
        assertDoesNotThrow { validator.validate("--secret", "secret123") }
    }

    @Test
    fun cyrillicSecretIsAccepted() {
        assertDoesNotThrow { validator.validate("--secret", "секрет") }
    }

    @Test
    fun nullSecretIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--secret", null)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("не указан"))
    }

    @Test
    fun emptySecretIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--secret", "")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("не указан"))
    }

    @Test
    fun whitespaceSecretIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--secret", "   ")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("не указан"))
    }

    @Test
    fun keyArgsHasNullSecretByDefault() {
        val keyArgs = KeyArgs()
        assertNull(keyArgs.secret)
    }
}
