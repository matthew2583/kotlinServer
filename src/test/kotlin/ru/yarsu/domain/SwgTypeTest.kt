package ru.yarsu.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class SwgTypeTest {
    @Test
    fun riverSandHasCorrectDensity() {
        assertEquals(1.5, SwgType.RIVER_SAND.density)
    }

    @Test
    fun quarrySandHasCorrectDensity() {
        assertEquals(1.5, SwgType.QUARRY_SAND.density)
    }

    @Test
    fun graniteRubbleHasCorrectDensity() {
        assertEquals(1.4, SwgType.GRANITE_RUBBLE.density)
    }

    @Test
    fun gravelRubbleHasCorrectDensity() {
        assertEquals(1.43, SwgType.GRAVEL_RUBBLE.density)
    }

    @Test
    fun slagRubbleHasCorrectDensity() {
        assertEquals(1.17, SwgType.RUSHED_STONE_SLAG.density)
    }

    @Test
    fun sandAndGravelMixHasCorrectDensity() {
        assertEquals(1.6, SwgType.SAND_AND_GRAVEL_MIX.density)
    }

    @ParameterizedTest
    @CsvSource(
        "RIVER_SAND, Песок речной",
        "QUARRY_SAND, Песок карьерный",
        "GRANITE_RUBBLE, Щебень гранитный",
        "GRAVEL_RUBBLE, Щебень гравийный",
        "RUSHED_STONE_SLAG, Щебень шлаковый",
        "SAND_AND_GRAVEL_MIX, Песчано-гравийная смесь",
    )
    fun swgTypeHasCorrectDisplayName(
        enumName: String,
        expectedDisplayName: String,
    ) {
        val swgType = SwgType.valueOf(enumName)
        assertEquals(expectedDisplayName, swgType.displayName)
    }

    @Test
    fun toStringReturnsDisplayName() {
        assertEquals("Песок речной", SwgType.RIVER_SAND.toString())
        assertEquals("Щебень гранитный", SwgType.GRANITE_RUBBLE.toString())
    }

    @ParameterizedTest
    @CsvSource(
        "Песок речной, RIVER_SAND",
        "Песок карьерный, QUARRY_SAND",
        "Щебень гранитный, GRANITE_RUBBLE",
        "Щебень гравийный, GRAVEL_RUBBLE",
        "Щебень шлаковый, RUSHED_STONE_SLAG",
        "Песчано-гравийная смесь, SAND_AND_GRAVEL_MIX",
    )
    fun fromStringParsesValidDisplayName(
        displayName: String,
        expectedEnum: String,
    ) {
        val result = SwgType.fromString(displayName)
        assertEquals(SwgType.valueOf(expectedEnum), result)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "Unknown", "песок речной", "RIVER_SAND", "invalid"])
    fun fromStringThrowsExceptionForInvalidValue(invalidValue: String) {
        val exception =
            assertThrows<IllegalArgumentException> {
                SwgType.fromString(invalidValue)
            }
        val message = exception.message ?: ""
        assertTrue(message.contains("Неизвестный тип ПГС"))
    }

    @Test
    fun swgTypeHasExactly6Entries() {
        assertEquals(6, SwgType.entries.size)
    }

    @Test
    fun allSwgTypesHavePositiveDensity() {
        SwgType.entries.forEach { swgType ->
            assertTrue(swgType.density > 0, "Density for ${swgType.name} should be positive")
        }
    }

    @Test
    fun allSwgTypesHaveNonEmptyDisplayName() {
        SwgType.entries.forEach { swgType ->
            assertTrue(swgType.displayName.isNotBlank(), "DisplayName for ${swgType.name} should not be blank")
        }
    }
}
