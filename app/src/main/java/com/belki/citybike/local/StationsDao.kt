package com.belki.citybike.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.belki.citybike.dto.NetworkStation


/**
 * Data Access Object for the stations table.
 */
@Dao
interface StationsDao {
    /**
     * @return all stations.
     */
    @Query("SELECT * FROM stations")
    suspend fun getStations(): List<NetworkStation>

    /**
     * Insert stations in the database. If the station already exists, replace it.
     *
     * @param stations varargs stations to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg stations: NetworkStation)
}