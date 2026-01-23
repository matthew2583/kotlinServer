package ru.yarsu.storage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import ru.yarsu.data.Trucks
import ru.yarsu.domain.SwgType
import java.util.UUID

class TrucksStorageTest {
    private lateinit var storage: TrucksStorage

    @BeforeEach
    fun setUp() {
        storage = TrucksStorage()
    }

    @Nested
    inner class FindOrCreateTruckTests {
        @Test
        fun findOrCreateCreatesNewTruck() {
            val (truck, created, message) =
                storage.findOrCreateTruck(
                    model = "КАМАЗ-6520",
                    registration = "А123БВ77",
                    weight = 20.0,
                    swgType = SwgType.RIVER_SAND,
                )

            assertNotNull(truck)
            assertTrue(created)
            assertNotNull(message)
        }

        @Test
        fun findOrCreateReturnsExistingTruck() {
            storage.findOrCreateTruck(
                model = "КАМАЗ-6520",
                registration = "А123БВ77",
                weight = 20.0,
                swgType = SwgType.RIVER_SAND,
            )

            val (truck, created, _) =
                storage.findOrCreateTruck(
                    model = "КАМАЗ-6520",
                    registration = "А123БВ77",
                    weight = 20.0,
                    swgType = SwgType.RIVER_SAND,
                )

            assertNotNull(truck)
            assertFalse(created)
        }

        @Test
        fun volumeCalculatedCorrectlyForNewTruck() {
            val weight = 30.0
            val swgType = SwgType.RIVER_SAND
            val expectedVolume = weight / swgType.density

            val (truck, _, _) =
                storage.findOrCreateTruck(
                    model = "Test",
                    registration = "TEST",
                    weight = weight,
                    swgType = swgType,
                )

            assertNotNull(truck)
            assertEquals(expectedVolume, truck?.volume ?: 0.0, 0.01)
        }
    }

    @Nested
    inner class GetTruckTests {
        @Test
        fun getTruckByIdReturnsNullForNonExistent() {
            val truck = storage.getTruckById(UUID.randomUUID())
            assertNull(truck)
        }

        @Test
        fun getAllTrucksReturnsEmptyListInitially() {
            val trucks = storage.getAllTrucks()
            assertTrue(trucks.isEmpty())
        }

        @Test
        fun getAllTrucksReturnsAllCreated() {
            repeat(3) { i ->
                storage.findOrCreateTruck(
                    model = "Model $i",
                    registration = "REG$i",
                    weight = 20.0,
                    swgType = SwgType.RIVER_SAND,
                )
            }

            val trucks = storage.getAllTrucks()
            assertEquals(3, trucks.size)
        }
    }

    @Nested
    inner class InitWithListTests {
        @Test
        fun storageCanBeInitializedWithList() {
            val truck =
                Trucks(
                    id = UUID.randomUUID(),
                    model = "КАМАЗ",
                    registration = "А123БВ77",
                    capacity = 20.0,
                    volume = 14.0,
                )

            val storageWithData = TrucksStorage(listOf(truck))

            assertEquals(1, storageWithData.getAllTrucks().size)
            assertNotNull(storageWithData.getTruckById(truck.id))
        }
    }
}
