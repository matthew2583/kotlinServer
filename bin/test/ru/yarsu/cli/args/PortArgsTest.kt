package ru.yarsu.cli.args

import com.beust.jcommander.ParameterException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PortArgsTest {
    private val validator = PortValidator()

    @Test
    fun validPortsAreAccepted() {
        val validPorts = listOf("1024", "8080", "3000", "9000", "65535")
        validPorts.forEach { port ->
            assertDoesNotThrow { validator.validate("--port", port) }
        }
    }

    @Test
    fun minimumPort1024IsAccepted() {
        assertDoesNotThrow { validator.validate("--port", "1024") }
    }

    @Test
    fun maximumPort65535IsAccepted() {
        assertDoesNotThrow { validator.validate("--port", "65535") }
    }

    @Test
    fun portBelow1024IsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--port", "1023")
            }
        assertNotNull(exception.message)
    }

    @Test
    fun zeroPortIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--port", "0")
            }
        assertNotNull(exception.message)
    }

    @Test
    fun portAbove65535IsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--port", "65536")
            }
        assertNotNull(exception.message)
    }

    @Test
    fun negativePortIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--port", "-1")
            }
        assertNotNull(exception.message)
    }

    @Test
    fun nonNumericPortIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--port", "abc")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("числом"))
    }

    @Test
    fun emptyPortIsRejected() {
        assertThrows<ParameterException> {
            validator.validate("--port", "")
        }
    }

    @Test
    fun nullPortIsRejected() {
        assertThrows<ParameterException> {
            validator.validate("--port", null)
        }
    }

    @Test
    fun decimalPortIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--port", "8080.5")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("числом"))
    }

    @Test
    fun portArgsHasDefaultValue9000() {
        val portArgs = PortArgs()
        assertEquals(9000, portArgs.ports)
    }
}
