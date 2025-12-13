package ru.yarsu.web.routes.validateV1

import org.http4k.core.Response
import ru.yarsu.internal.SwgType
import ru.yarsu.web.routes.util.GetResponse
import java.time.LocalDate
import java.time.format.DateTimeParseException

object QueryValidation {
    private val allowedRecordsPerPage = setOf(5, 10, 20, 50)

    private fun error(message: String): Response = GetResponse.responseBadRequest(message)

    fun validatePage(pageStr: String?): Response? {
        if (pageStr.isNullOrBlank()) {
            return error("Некорректное значение параметра page: пусто или пробелы")
        }
        val page = pageStr.toIntOrNull()
        if (page == null || page <= 0) {
            return error("Некорректное значение параметра page: ожидается натуральное число, но получено $pageStr")
        }
        return null
    }

    fun validateRecordsPerPage(recordsStr: String?): Response? {
        if (recordsStr.isNullOrBlank()) {
            return error("Некорректное значение параметра records-per-page: пусто или пробелы")
        }
        val rec = recordsStr.toIntOrNull()
        if (rec == null || rec !in allowedRecordsPerPage) {
            return error(
                "Некорректное значение параметра records-per-page: " +
                    "ожидается одно из: ${allowedRecordsPerPage.joinToString()}, но получено: $recordsStr",
            )
        }
        return null
    }

    fun validateToPeriod(
        fromStr: String?,
        toStr: String?,
    ): Pair<Response?, Pair<LocalDate, LocalDate>?> {
        if (fromStr.isNullOrBlank()) {
            return error("Некорректное значение параметра from: параметр отсутствует") to null
        }
        if (toStr.isNullOrBlank()) {
            return error("Некорректное значение параметра to: параметр отсутствует") to null
        }

        val from: LocalDate =
            try {
                LocalDate.parse(fromStr)
            } catch (_: DateTimeParseException) {
                return error(
                    "Некорректное значение параметра from: дата передана в некорректном формате",
                ) to null
            }

        val to: LocalDate =
            try {
                LocalDate.parse(toStr)
            } catch (_: DateTimeParseException) {
                return error(
                    "Некорректное значение параметра to: дата передана в некорректном формате",
                ) to null
            }

        if (from > to) {
            return error(
                "Некорректные значения параметров from и to: дата начала не может быть позже даты окончания",
            ) to null
        }
        return null to (from to to)
    }

    fun validateBySwgType(swgStr: String?): Pair<Response?, SwgType?> {
        if (swgStr.isNullOrBlank()) {
            return error(
                "Некорректное значение параметра by-swg-type: значение параметра пусто",
            ) to null
        }

        return try {
            null to SwgType.fromString(swgStr.trim())
        } catch (_: IllegalArgumentException) {
            error(
                "Некорректное значение параметра by-swg-type: " +
                    "ожидается одно из: ${SwgType.entries.joinToString(", ") { it.displayName }}," +
                    " но получено: $swgStr",
            ) to null
        }
    }

    fun validateYear(yearStr: String?): Pair<Response?, Int?> {
        if (yearStr.isNullOrBlank()) {
            return error("Некорректное значение параметра year: значение параметра пусто") to null
        }

        val year =
            try {
                yearStr.toInt()
            } catch (_: Exception) {
                return error(
                    "Некорректное значение параметра year. " +
                        "Ожидалось целое положительное число (>= 2000), но получено " + yearStr,
                ) to null
            }
        if (year < 2000) {
            return error(
                "Некорректное значение параметра year. Ожидалось целое положительное число (>= 2000), но получено " +
                    yearStr,
            ) to null
        }
        return null to year
    }
}
