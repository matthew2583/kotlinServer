package ru.yarsu.cli.args

import com.beust.jcommander.IParameterValidator
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.beust.jcommander.Parameters

@Parameters(separators = "=")
class PortArgs {
    @Parameter(
        names = ["--port"],
        required = true,
        validateWith = [PortValidator::class],
    )
    var ports: Int = 9000
}

class PortValidator : IParameterValidator {
    override fun validate(
        name: String?,
        value: String?,
    ) {
        try {
            val port = value?.toInt() ?: throw ParameterException("Порт не может быть пустым")

            if (port !in 1024..65535) {
                throw ParameterException("Неверный порт: $port. Допустимые значения 1024–65535.")
            }
        } catch (_: NumberFormatException) {
            throw ParameterException("Порт должен быть числом, получено: $value")
        }
    }
}
