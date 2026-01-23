package ru.yarsu.cli.args

import com.beust.jcommander.IParameterValidator
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.beust.jcommander.Parameters

@Parameters(separators = "=")
class KeyArgs {
    @Parameter(
        names = ["--secret"],
        required = true,
        validateWith = [SecretValidator::class],
    )
    var secret: String? = null
}

class SecretValidator : IParameterValidator {
    override fun validate(
        name: String?,
        value: String?,
    ) {
        if (value.isNullOrBlank()) {
            throw ParameterException("Секретный ключ не указан")
        }
    }
}
