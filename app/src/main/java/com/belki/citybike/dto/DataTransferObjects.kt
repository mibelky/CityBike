package com.belki.citybike.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*
import com.belki.citybike.domain.Station

/**
 * Model class for Station as the object for receiving from RESTApi and storing with Room
 *
 * @param id            id of the station
 * @param emptySlots    number of empty slots
 * @param freeBikes     number of free bikes
 * @param latitude      latitude of the station location
 * @param longitude     longitude of the station location
 * @param name          name of the station
 */

@Entity(tableName = "stations")
data class NetworkStation(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "emptySlots") val emptySlots: Int,
    @ColumnInfo(name = "freeBikes") val freeBikes: Int,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "name") val name: String
)

/**
 *  Extension function that convert NetworkStation into the list of stations
 */
fun NetworkStation.asStation() = Station(
    id = id,
    emptySlots = emptySlots,
    freeBikes = freeBikes,
    latitude = latitude,
    longitude = longitude,
    name = name,
    distance = 0
)

