package ru.yarsu.cli.args

import com.beust.jcommander.IParameterValidator
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.beust.jcommander.Parameters
import java.io.File

@Parameters(separators = "=")
class PathArgs {
    @Parameter(
        names = ["--swg-file"],
        required = true,
        validateWith = [PathValidator::class],
    )
    var swgFile: String? = null

    @Parameter(
        names = ["--dump-trucks-file"],
        required = true,
        validateWith = [PathValidator::class],
    )
    var dumpTrucksFile: String? = null

    @Parameter(
        names = ["--employees-file"],
        required = true,
        validateWith = [PathValidator::class],
    )
    var employeesFile: String? = null
}

class PathValidator : IParameterValidator {
    override fun validate(
        name: String?,
        value: String?,
    ) {
        if (value.isNullOrBlank()) {
            throw ParameterException("Ошибка: путь к файлу не указан")
        }

        val file = File(value)
        if (!file.exists()) {
            throw ParameterException("Ошибка: файл не найден - $value")
        }
        if (!file.isFile) {
            throw ParameterException("Ошибка: указанный путь не является файлом - $value")
        }
        if (!file.canRead()) {
            throw ParameterException("Ошибка: указанный файл недоступен для чтения - $value")
        }
        if (file.length().toInt() == 0) {
            throw ParameterException("Ошибка: файл пуст")
        }
    }
}
