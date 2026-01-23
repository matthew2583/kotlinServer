package ru.yarsu.cli.args

import com.beust.jcommander.ParameterException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class YearArgsTest {
    private val validator = YearValidator()

    @Test
    fun validYearsAreAccepted() {
        val validYears = listOf("2024", "2023", "1990", "2099", "1")
        validYears.forEach { year ->
            assertDoesNotThrow { validator.validate("--year", year) }
        }
    }

    @Test
    fun largeYearValueIsAccepted() {
        assertDoesNotThrow { validator.validate("--year", "9999") }
    }

    @Test
    fun nullYearIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--year", null)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("не указан"))
    }

    @Test
    fun emptyYearIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--year", "")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("не указан"))
    }

    @Test
    fun whitespaceYearIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--year", "   ")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("не указан"))
    }

    @Test
    fun zeroYearIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--year", "0")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("положительным"))
    }

    @Test
    fun negativeYearIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--year", "-1")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("положительным"))
    }

    @Test
    fun nonNumericYearIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--year", "abc")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("целым"))
    }

    @Test
    fun decimalYearIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                validator.validate("--year", "2024.5")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("целым"))
    }

    @Test
    fun yearArgsHasZeroByDefault() {
        val yearArgs = YearArgs()
        assertEquals(0, yearArgs.yearArgs)
    }
}
