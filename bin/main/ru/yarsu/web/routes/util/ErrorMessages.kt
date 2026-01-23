package ru.yarsu.web.routes.util

object ErrorMessages {
    const val UNAUTHORIZED = "Отказано в авторизации"
    const val FORBIDDEN = "Недостаточно прав для выполнения операции"
    const val INVALID_JSON = "Тело запроса не является JSON-документом"
    const val INVALID_FORM = "Тело запроса не является form-urlencoded"
    const val CONVERSION_ERROR = "Неизвестная ошибка при преобразовании запроса"
    const val FORM_CONVERSION_ERROR = "Неизвестная ошибка при преобразовании формы"
    const val TRUCK_NOT_FOUND = "Самосвал не найден"
    const val SHIPMENT_NOT_FOUND = "Акт отгрузки ПГС не найден"
    const val INVALID_TRUCK_ID = "Некорректный идентификатор самосвала"
    const val INVALID_SHIPMENT_ID = "Некорректный идентификатор акта отгрузки"
    const val EMPLOYEE_NOT_FOUND = "Работник с указанным ID не найден"
    const val CANNOT_CHANGE_MANAGER =
        "Роль пользователя, выполнившего запрос, не позволяет обновить отгрузку с переданным пользователем."
    const val EMPLOYEE_NOT_REGISTERED =
        "Невозможно обновить отгрузку, так как работник не был зарегистрирован в системе " +
            "(ShipmentDateTime < Employee->RegistrationDateTime)"
    const val EMPLOYEE_NOT_REGISTERED_CREATE =
        "Невозможно создать отгрузку, так как работник не был зарегистрирован в системе " +
            "(ShipmentDateTime < Employee->RegistrationDateTime)"
}
