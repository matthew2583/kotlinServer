package ru.yarsu.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class TrucksTest {
    @Test
    fun truckCreatedWithAllProperties() {
        val id = UUID.randomUUID()

        val truck =
            Trucks(
                id = id,
                model = "КАМАЗ-6520",
                registration = "А123БВ77",
                capacity = 20.0,
                volume = 14.0,
            )

        assertEquals(id, truck.id)
        assertEquals("КАМАЗ-6520", truck.model)
        assertEquals("А123БВ77", truck.registration)
        assertEquals(20.0, truck.capacity)
        assertEquals(14.0, truck.volume)
    }

    @Test
    fun dataClassImplementsEqualsCorrectly() {
        val id = UUID.randomUUID()

        val truck1 =
            Trucks(
                id = id,
                model = "МАЗ",
                registration = "В456ГД78",
                capacity = 25.0,
                volume = 18.0,
            )

        val truck2 =
            Trucks(
                id = id,
                model = "МАЗ",
                registration = "В456ГД78",
                capacity = 25.0,
                volume = 18.0,
            )

        assertEquals(truck1, truck2)
        assertEquals(truck1.hashCode(), truck2.hashCode())
    }

    @Test
    fun trucksWithDifferentIdsAreNotEqual() {
        val truck1 =
            Trucks(
                id = UUID.randomUUID(),
                model = "Volvo",
                registration = "Е789ЖЗ99",
                capacity = 30.0,
                volume = 22.0,
            )

        val truck2 =
            Trucks(
                id = UUID.randomUUID(),
                model = "Volvo",
                registration = "Е789ЖЗ99",
                capacity = 30.0,
                volume = 22.0,
            )

        assertNotEquals(truck1, truck2)
    }

    @Test
    fun copyCreatesModifiedCopy() {
        val truck =
            Trucks(
                id = UUID.randomUUID(),
                model = "КАМАЗ",
                registration = "А000АА00",
                capacity = 15.0,
                volume = 10.0,
            )

        val copied = truck.copy(model = "МАЗ", capacity = 20.0)

        assertEquals("МАЗ", copied.model)
        assertEquals(20.0, copied.capacity)
        assertEquals(truck.id, copied.id)
        assertEquals(truck.registration, copied.registration)
        assertEquals(truck.volume, copied.volume)
    }

    @Test
    fun handlesDecimalCapacityAndVolume() {
        val truck =
            Trucks(
                id = UUID.randomUUID(),
                model = "DAF",
                registration = "К111ЛМ55",
                capacity = 17.5,
                volume = 12.357,
            )

        assertEquals(17.5, truck.capacity)
        assertEquals(12.357, truck.volume)
    }

    @Test
    fun supportsCyrillicModelAndRegistration() {
        val truck =
            Trucks(
                id = UUID.randomUUID(),
                model = "КАМАЗ",
                registration = "А123БВ77",
                capacity = 20.0,
                volume = 14.0,
            )

        assertEquals("КАМАЗ", truck.model)
        assertEquals("А123БВ77", truck.registration)
    }
}
