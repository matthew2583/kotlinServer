package ru.yarsu.cli.args

import com.beust.jcommander.ParameterException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.yarsu.domain.SwgType

class SwgTypeArgsTest {
    private val converter = SwgTypeConverter()

    @Test
    fun riverSandIsConvertedCorrectly() {
        val result = converter.convert("Песок речной")
        assertEquals(SwgType.RIVER_SAND, result)
    }

    @Test
    fun quarrySandIsConvertedCorrectly() {
        val result = converter.convert("Песок карьерный")
        assertEquals(SwgType.QUARRY_SAND, result)
    }

    @Test
    fun graniteRubbleIsConvertedCorrectly() {
        val result = converter.convert("Щебень гранитный")
        assertEquals(SwgType.GRANITE_RUBBLE, result)
    }

    @Test
    fun gravelRubbleIsConvertedCorrectly() {
        val result = converter.convert("Щебень гравийный")
        assertEquals(SwgType.GRAVEL_RUBBLE, result)
    }

    @Test
    fun slagRubbleIsConvertedCorrectly() {
        val result = converter.convert("Щебень шлаковый")
        assertEquals(SwgType.RUSHED_STONE_SLAG, result)
    }

    @Test
    fun sandAndGravelMixIsConvertedCorrectly() {
        val result = converter.convert("Песчано-гравийная смесь")
        assertEquals(SwgType.SAND_AND_GRAVEL_MIX, result)
    }

    @Test
    fun nullValueThrowsParameterException() {
        assertThrows<ParameterException> {
            converter.convert(null)
        }
    }

    @Test
    fun emptyValueThrowsParameterException() {
        assertThrows<ParameterException> {
            converter.convert("")
        }
    }

    @Test
    fun whitespaceValueThrowsParameterException() {
        assertThrows<ParameterException> {
            converter.convert("   ")
        }
    }

    @Test
    fun unknownSwgTypeThrowsIllegalArgumentException() {
        assertThrows<IllegalArgumentException> {
            converter.convert("Неизвестный тип")
        }
    }

    @Test
    fun caseMatters() {
        assertThrows<IllegalArgumentException> {
            converter.convert("песок речной")
        }
    }

    @Test
    fun enumNameIsNotAcceptedInsteadOfDisplayName() {
        assertThrows<IllegalArgumentException> {
            converter.convert("RIVER_SAND")
        }
    }

    @Test
    fun swgTypeArgsHasNullByDefault() {
        val swgTypeArgs = SwgTypeArgs()
        assertNull(swgTypeArgs.swgType)
    }
}
