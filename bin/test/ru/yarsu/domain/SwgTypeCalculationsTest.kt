package ru.yarsu.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal

class SwgTypeCalculationsTest {
    @Nested
    inner class VolumeCalculationTests {
        @Test
        fun volumeCalculatedCorrectlyForRiverSand() {
            val weight = 30.0
            val expectedVolume = weight / SwgType.RIVER_SAND.density
            assertEquals(20.0, expectedVolume, 0.01)
        }

        @Test
        fun volumeCalculatedCorrectlyForGraniteRubble() {
            val weight = 28.0
            val expectedVolume = weight / SwgType.GRANITE_RUBBLE.density
            assertEquals(20.0, expectedVolume, 0.01)
        }

        @Test
        fun volumeCalculatedCorrectlyForSandAndGravelMix() {
            val weight = 32.0
            val expectedVolume = weight / SwgType.SAND_AND_GRAVEL_MIX.density
            assertEquals(20.0, expectedVolume, 0.01)
        }

        @ParameterizedTest
        @EnumSource(SwgType::class)
        fun volumeIsPositiveForPositiveWeight(swgType: SwgType) {
            val weight = 100.0
            val volume = weight / swgType.density
            assertTrue(volume > 0)
        }

        @ParameterizedTest
        @EnumSource(SwgType::class)
        fun weightEqualsVolumTimesDensity(swgType: SwgType) {
            val weight = 50.0
            val volume = weight / swgType.density
            val calculatedWeight = volume * swgType.density
            assertEquals(weight, calculatedWeight, 0.0001)
        }
    }

    @Nested
    inner class DensityComparisonTests {
        @Test
        fun sandAndGravelMixHasHighestDensity() {
            val maxDensity = SwgType.entries.maxOf { it.density }
            assertEquals(SwgType.SAND_AND_GRAVEL_MIX.density, maxDensity)
        }

        @Test
        fun slagRubbleHasLowestDensity() {
            val minDensity = SwgType.entries.minOf { it.density }
            assertEquals(SwgType.RUSHED_STONE_SLAG.density, minDensity)
        }

        @Test
        fun riverSandAndQuarrySandHaveSameDensity() {
            assertEquals(SwgType.RIVER_SAND.density, SwgType.QUARRY_SAND.density)
        }

        @Test
        fun densitiesAreInReasonableRangeForConstructionMaterials() {
            SwgType.entries.forEach { swgType ->
                assertTrue(swgType.density >= 1.0, "${swgType.name} density too low")
                assertTrue(swgType.density <= 3.0, "${swgType.name} density too high")
            }
        }
    }

    @Nested
    inner class CostCalculationTests {
        @Test
        fun costDoesNotExceedPriceTimesCountDividedBy1000() {
            val count = BigDecimal("100")
            val price = BigDecimal("500")
            val maxCost = price.multiply(count).divide(BigDecimal(1000))
            assertEquals(BigDecimal("50"), maxCost)
        }

        @Test
        fun maxAllowedCostCalculatedCorrectly() {
            val testCases =
                listOf(
                    Triple(BigDecimal("36"), BigDecimal("640"), BigDecimal("23.04")),
                    Triple(BigDecimal("100"), BigDecimal("500"), BigDecimal("50")),
                    Triple(BigDecimal("200"), BigDecimal("1000"), BigDecimal("200")),
                )

            testCases.forEach { (count, price, expectedMaxCost) ->
                val maxCost = price.multiply(count).divide(BigDecimal(1000))
                assertEquals(expectedMaxCost, maxCost)
            }
        }
    }
}
