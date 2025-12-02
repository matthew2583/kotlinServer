package ru.yarsu.args

import com.beust.jcommander.IParameterValidator
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.beust.jcommander.Parameters
import java.util.UUID

@Parameters(separators = "=")
class ShipmentIdArgs {
    @Parameter(
        names = ["--shipment-id"],
        required = true,
        validateWith = [UUIDValidator::class],
    )
    var shipmentID: String? = null
}

class UUIDValidator : IParameterValidator {
    override fun validate(
        name: String?,
        value: String?,
    ) {
        if (value.isNullOrBlank()) {
            throw ParameterException("Ошибка: значение UUID не было передано в аргументы")
        }
        try {
            UUID.fromString(value)
        } catch (ex: IllegalArgumentException) {
            throw ParameterException("Ошибка: некорректный UUID передан в аргументы $value")
        }
    }
}
