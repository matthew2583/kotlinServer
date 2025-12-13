package ru.yarsu.args

import com.beust.jcommander.IStringConverter
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.beust.jcommander.Parameters
import ru.yarsu.internal.SwgType

@Parameters(separators = "=")
class SwgTypeArgs {
    @Parameter(
        names = ["--type"],
        required = true,
        converter = SwgTypeConverter::class,
    )
    var swgType: SwgType? = null
}

class SwgTypeConverter : IStringConverter<SwgType> {
    override fun convert(value: String?): SwgType {
        if (value.isNullOrBlank()) {
            throw ParameterException("Ошибка: в аргументы не передано значение типа ПГС")
        }
        return SwgType.fromString(value)
    }
}
