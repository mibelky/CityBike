package com.belki.citybike.local

import android.content.Context
import androidx.room.Room


/**
 * Singleton class that is used to create a station db
 */
object LocalDB {

    /**
     * static method that creates a NetworkStation class and returns the DAO of the station
     */
    fun createStationsDao(context: Context): StationsDao {
        return Room.databaseBuilder(
            context.applicationContext,
            StationsDatabase::class.java, "stations.db"
        ).build().stationDao()
    }

}