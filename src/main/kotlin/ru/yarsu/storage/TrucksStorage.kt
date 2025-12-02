package ru.yarsu.storage

import ru.yarsu.data.Trucks
import ru.yarsu.internal.SwgType
import java.util.UUID

class TrucksStorage(
    initialTrucks: List<Trucks> = emptyList(),
) {
    private val trucksList = initialTrucks.toMutableList()

    fun getAllTrucks(): List<Trucks> = trucksList.toList()

    fun getTruckById(id: UUID?): Trucks? = trucksList.find { it.id == id }

    fun getTrucksByExactModel(model: String): List<Trucks> = trucksList.filter { it.model == model }

    fun getTruckByExactRegistration(registration: String): Trucks? = trucksList.find { it.registration == registration }

    fun getTrucksByExactCapacity(capacity: Double): List<Trucks> = trucksList.filter { it.capacity == capacity }

    fun getTrucksByExactVolume(volume: Double): List<Trucks> = trucksList.filter { it.volume == volume }

    fun addTrucks(
        model: String,
        registration: String,
        capacity: Double,
        volume: Double,
    ): UUID {
        val newTruck =
            Trucks(
                id = UUID.randomUUID(),
                model = model,
                registration = registration,
                capacity = capacity,
                volume = volume,
            )
        trucksList.add(newTruck)
        return newTruck.id
    }

    fun updateTruck(
        id: UUID,
        model: String,
        registration: String,
        capacity: Double,
        volume: Double,
    ): Boolean {
        val index = trucksList.indexOfFirst { it.id == id }
        if (index == -1) return false

        val updatedTruck =
            Trucks(
                id = id,
                model = model,
                registration = registration,
                capacity = capacity,
                volume = volume,
            )

        trucksList[index] = updatedTruck
        return true
    }

    fun deleteTruck(id: UUID): Boolean = trucksList.removeIf { it.id == id }

    fun findTrucksByModelAndRegistration(
        model: String,
        registration: String,
    ): List<Trucks> =
        trucksList
            .filter {
                it.model == model && it.registration == registration
            }.sortedWith(compareBy({ it.capacity }, { it.id }))

    fun addTruck(truck: Trucks): UUID {
        trucksList.add(truck)
        return truck.id
    }

    fun findOrCreateTruck(
        model: String,
        registration: String,
        weight: Double,
        swgType: SwgType,
    ): Triple<Trucks?, Boolean, List<Trucks>> {
        val matchingTrucks = findTrucksByModelAndRegistration(model, registration)

        return when {
            matchingTrucks.isEmpty() -> {
                val volume = weight / swgType.density
                val newTruck =
                    Trucks(
                        id = UUID.randomUUID(),
                        model = model,
                        registration = registration,
                        capacity = weight,
                        volume = volume,
                    )
                addTruck(newTruck)
                Triple(newTruck, true, emptyList())
            }

            matchingTrucks.size == 1 -> {
                Triple(matchingTrucks.first(), false, emptyList())
            }

            else -> {
                Triple(null, false, matchingTrucks)
            }
        }
    }
}
