package ru.yarsu.cli.args

import com.beust.jcommander.ParameterException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PeriodArgsTest {
    private val fromValidator = FromDateValidator()
    private val toValidator = ToDateValidator()

    @Test
    fun validDatesAreAcceptedForFrom() {
        val validDates = listOf("2024-01-01", "2023-12-31", "2025-06-15", "1990-01-01")
        validDates.forEach { date ->
            assertDoesNotThrow { fromValidator.validate("--from", date) }
        }
    }

    @Test
    fun leapYearIsHandledCorrectly() {
        assertDoesNotThrow { fromValidator.validate("--from", "2024-02-29") }
    }

    @Test
    fun nullFromDateIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                fromValidator.validate("--from", null)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("--from") && message.contains("не указан"))
    }

    @Test
    fun emptyFromDateIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                fromValidator.validate("--from", "")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("--from"))
    }

    @Test
    fun invalidDateFormatIsRejectedForFrom() {
        val exception =
            assertThrows<ParameterException> {
                fromValidator.validate("--from", "01-01-2024")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("YYYY-MM-DD"))
    }

    @Test
    fun slashSeparatorDateIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                fromValidator.validate("--from", "2024/01/01")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("YYYY-MM-DD"))
    }

    @Test
    fun invalidDayIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                fromValidator.validate("--from", "2024-01-32")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("YYYY-MM-DD"))
    }

    @Test
    fun feb29InNonLeapYearIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                fromValidator.validate("--from", "2023-02-29")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("YYYY-MM-DD"))
    }

    @Test
    fun validDatesAreAcceptedForTo() {
        val validDates = listOf("2024-01-01", "2023-12-31", "2025-06-15")
        validDates.forEach { date ->
            assertDoesNotThrow { toValidator.validate("--to", date) }
        }
    }

    @Test
    fun nullToDateIsRejected() {
        val exception =
            assertThrows<ParameterException> {
                toValidator.validate("--to", null)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("--to"))
    }

    @Test
    fun invalidDateFormatIsRejectedForTo() {
        val exception =
            assertThrows<ParameterException> {
                toValidator.validate("--to", "not-a-date")
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("YYYY-MM-DD"))
    }

    @Test
    fun periodArgsHasNullValuesByDefault() {
        val periodArgs = PeriodArgs()
        assertNull(periodArgs.periodFromArgs)
        assertNull(periodArgs.periodToArgs)
    }
}
