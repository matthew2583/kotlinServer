package ru.yarsu.args

import com.beust.jcommander.IParameterValidator
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.beust.jcommander.Parameters
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Parameters(separators = "=")
class PeriodArgs {
    @Parameter(
        names = ["--from"],
        required = true,
        validateWith = [FromDateValidator::class],
    )
    var periodFromArgs: String? = null

    @Parameter(
        names = ["--to"],
        required = true,
        validateWith = [ToDateValidator::class],
    )
    var periodToArgs: String? = null
}

class FromDateValidator : IParameterValidator {
    override fun validate(
        name: String?,
        value: String?,
    ) {
        if (value.isNullOrBlank()) {
            throw ParameterException("Ошибка: параметр --from не указан или пуст")
        }
        try {
            LocalDate.parse(value)
        } catch (ex: DateTimeParseException) {
            throw ParameterException("Ошибка: параметр --from должен быть датой в формате YYYY-MM-DD: $value")
        }
    }
}

class ToDateValidator : IParameterValidator {
    override fun validate(
        name: String?,
        value: String?,
    ) {
        if (value.isNullOrBlank()) {
            throw ParameterException("Ошибка: параметр --to не указан или пуст")
        }
        try {
            LocalDate.parse(value)
        } catch (ex: DateTimeParseException) {
            throw ParameterException("Ошибка: параметр --to должен быть датой в формате YYYY-MM-DD: $value")
        }
    }
}
