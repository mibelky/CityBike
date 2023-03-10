package com.belki.citybike.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.belki.citybike.dto.NetworkStation

/**
 * The Room Database that contains the stations table.
 */
@Database(entities = [NetworkStation::class], version = 1, exportSchema = false)
abstract class StationsDatabase : RoomDatabase() {

    abstract fun stationDao(): StationsDao
}