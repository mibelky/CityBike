package com.belki.citybike.remote

import android.util.Log
import com.belki.citybike.dto.NetworkStation
import org.json.JSONException
import org.json.JSONObject

fun JSONObject.parseToStationsArray(): List<NetworkStation> {
    val network = getJSONObject("network")
    val stationsList = ArrayList<NetworkStation>()
    val stationsJsonArray = network.getJSONArray("stations")
    Log.d("JSON", "Stations array length is ${stationsJsonArray.length()}")

    for (i in 0 until stationsJsonArray.length()) {
        val stationJson = stationsJsonArray.getJSONObject(i)

        var emptySlots = 0
        var exceptionCaught = false
        try {
            emptySlots = stationJson.getInt("empty_slots")
        } catch (e: JSONException) {
            exceptionCaught = true
            Log.e("JSON", "JSONException: ${e.message}")
        }
        val freeBikes: Int = stationJson.getInt("free_bikes")
        val id: String = stationJson.getString("id")
        val latitude: Double = stationJson.getDouble("latitude")
        val longitude: Double = stationJson.getDouble("longitude")
        val name: String = stationJson.getString("name").substringAfter('-')
        if (exceptionCaught) {
            Log.d("JSON", "Station inactive info:\n" +
                    "free bikes: $freeBikes\n" +
                    "id: $id\n" +
                    "latitude: $latitude\n" +
                    "longitude: $longitude\n" +
                    "name: $name")
        }

        val station = NetworkStation(
            id = id,
            emptySlots = emptySlots,
            freeBikes = freeBikes,
            latitude = latitude,
            longitude = longitude,
            name = name
        )

        stationsList.add(station)
    }
    return stationsList
}


