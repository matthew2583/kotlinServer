package ru.yarsu.internal

enum class Roles(
    val description: String,
) {
    Employee("Зарегистрированный работник"),
    Manager("Менеджер"),
    UserManager("Менеджер пользователей приложения"),
    ;

    companion object {
        fun fromString(value: String): Roles =
            entries.firstOrNull { it.name == value }
                ?: throw IllegalArgumentException(
                    "Недопустимая роль: $value",
                )
    }
}
