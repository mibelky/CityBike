package com.belki.citybike.local

import com.belki.citybike.dto.NetworkStation
import com.belki.citybike.dto.Result

/**
 * Main entry point for accessing reminders data.
 */
interface StationDataSource {
    suspend fun getStations(): Result<List<NetworkStation>>
    suspend fun saveStations(stations: List<NetworkStation>)
}