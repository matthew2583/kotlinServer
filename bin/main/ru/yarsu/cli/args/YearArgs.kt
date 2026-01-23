package ru.yarsu.cli.args

import com.beust.jcommander.IParameterValidator
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.beust.jcommander.Parameters

@Parameters(separators = "=")
class YearArgs {
    @Parameter(
        names = ["--year"],
        required = true,
        validateWith = [YearValidator::class],
    )
    var yearArgs: Int = 0
}

class YearValidator : IParameterValidator {
    override fun validate(
        name: String?,
        value: String?,
    ) {
        if (value.isNullOrBlank()) {
            throw ParameterException("Ошибка: не указан год отчёта")
        }
        val year =
            value.toIntOrNull()
                ?: throw ParameterException("Ошибка: параметр --year должен быть целым положительным числом: $value")
        if (year <= 0) {
            throw ParameterException("Ошибка: параметр --year должен быть положительным числом: $value")
        }
    }
}
