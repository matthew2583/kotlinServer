package ru.yarsu.web.routes.dto.json

typealias FieldError = Map<String, Any?>
typealias Errors = MutableMap<String, FieldError>

fun fieldError(
    value: Any?,
    message: String,
): FieldError = mapOf("Value" to value, "Error" to message)

fun putError(
    errors: Errors,
    field: String,
    value: Any?,
    message: String,
) {
    errors[field] = fieldError(value, message)
}

fun requireString(
    errors: Errors,
    field: String,
    value: Any?,
    requiredMessage: String = "Поле обязательно",
    typeMessage: String = "Ожидается строковое значение",
) {
    when (value) {
        null -> putError(errors, field, null, requiredMessage)
        !is String -> putError(errors, field, value, typeMessage)
        else -> {}
    }
}

fun requireNonBlankString(
    errors: Errors,
    field: String,
    value: Any?,
    requiredMessage: String = "Поле обязательно",
    typeMessage: String = "Ожидается строковое значение",
    blankMessage: String = "Ожидается непустая строка",
) {
    requireString(errors, field, value, requiredMessage, typeMessage)
    if (errors.containsKey(field)) return
    if ((value as String).isBlank()) {
        putError(errors, field, value, blankMessage)
    }
}

fun requireParsedNumber(
    errors: Errors,
    field: String,
    value: Any?,
    parseFn: (Any?) -> Any?,
    requiredMessage: String = "Поле обязательно",
    typeMessage: String = "Ожидается числовое значение",
    invalidMessage: String = "Ожидается положительное число",
) {
    when {
        value == null -> putError(errors, field, null, requiredMessage)
        value !is String && value !is Number -> putError(errors, field, value, typeMessage)
        parseFn(value) == null -> putError(errors, field, value, invalidMessage)
        else -> {}
    }
}

fun requireParsedUuidString(
    errors: Errors,
    field: String,
    value: Any?,
    parseFn: (Any?) -> Any?,
    requiredMessage: String = "Поле обязательно",
    typeMessage: String = "Ожидается строковое значение",
    invalidMessage: String = "Ожидается корректный UUID",
) {
    when {
        value == null -> putError(errors, field, null, requiredMessage)
        value !is String -> putError(errors, field, value, typeMessage)
        parseFn(value) == null -> putError(errors, field, value, invalidMessage)
        else -> {}
    }
}

fun requireParsedString(
    errors: Errors,
    field: String,
    value: Any?,
    parseFn: (String) -> Any?,
    requiredMessage: String = "Поле обязательно",
    typeMessage: String = "Ожидается строковое значение",
    invalidMessage: String,
) {
    requireString(errors, field, value, requiredMessage, typeMessage)
    if (errors.containsKey(field)) return
    val str = value as String
    if (parseFn(str) == null) {
        putError(errors, field, value, invalidMessage)
    }
}

fun validateDateTimeLikeYourCurrent(
    errors: Errors,
    field: String,
    value: Any?,
    parseFn: () -> Any?,
    nullMessage: String = "Поле должно быть строкой или отсутствовать",
    typeMessage: String = "Ожидается строковое значение",
    invalidMessage: String = "Ожидается корректная дата и время",
) {
    when {
        value == null -> putError(errors, field, null, nullMessage)
        value !is String -> putError(errors, field, value, typeMessage)
        parseFn() == null -> putError(errors, field, value, invalidMessage)
        else -> {}
    }
}

fun validateBooleanLikeYourCurrent(
    errors: Errors,
    field: String,
    value: Any?,
    nullMessage: String = "Поле должно быть булевым или отсутствовать",
    typeMessage: String = "Ожидается булево значение",
    invalidMessage: String = "Ожидается 'true' или 'false'",
) {
    when {
        value == null -> putError(errors, field, null, nullMessage)
        value !is String && value !is Boolean -> putError(errors, field, value, typeMessage)
        value is String && value.toBooleanStrictOrNull() == null -> putError(errors, field, value, invalidMessage)
        else -> {}
    }
}
