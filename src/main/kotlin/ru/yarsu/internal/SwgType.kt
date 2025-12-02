package ru.yarsu.internal

enum class SwgType(
    val density: Double,
    val displayName: String,
) {
    RIVER_SAND(1.5, "Песок речной"),
    QUARRY_SAND(1.5, "Песок карьерный"),
    GRANITE_RUBBLE(1.4, "Щебень гранитный"),
    GRAVEL_RUBBLE(1.43, "Щебень гравийный"),
    RUSHED_STONE_SLAG(1.17, "Щебень шлаковый"),
    SAND_AND_GRAVEL_MIX(1.6, "Песчано-гравийная смесь"),
    ;

    override fun toString(): String = displayName

    companion object {
        fun fromString(value: String): SwgType =
            entries.find { it.displayName == value }
                ?: throw IllegalArgumentException("Неизвестный тип ПГС: $value")
    }
}
