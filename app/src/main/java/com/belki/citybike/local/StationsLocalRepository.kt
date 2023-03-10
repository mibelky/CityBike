package com.belki.citybike.local

import androidx.lifecycle.MutableLiveData
import com.belki.citybike.dto.NetworkStation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.belki.citybike.dto.Result

/**
 * Concrete implementation of a data source as a db.
 *
 * The repository is implemented so that you can focus on only testing it.
 *
 * @param stationsDao the dao that does the Room db operations
 * @param ioDispatcher a coroutine dispatcher to offload the blocking IO tasks
 */
class StationsLocalRepository(
    private val stationsDao: StationsDao,
    private val stationsRetrofitService: StationDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : StationDataSource {

    val errorRemoteLoading = MutableLiveData<Result.Error?>()

    /**
     * Get the stations list from the local db
     * @return Result the holds a Success with all the stations or an Error object with the error message
     */
    override suspend fun getStations(): Result<List<NetworkStation>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(stationsDao.getStations())
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }
    }

    /**
     * Insert stations in the db.
     * @param stations stations to be inserted
     */
    override suspend fun saveStations(stations: List<NetworkStation>) =
        withContext(ioDispatcher) {
            stationsDao.insertAll(* stations.toTypedArray())
        }


    /**
     * Getting stations from remote service and loading it db in case of Success. Otherwise, saving Result.Error to errorRemoteLoading.
     */
    suspend fun updateStations() = withContext(ioDispatcher) {
        when (val result = stationsRetrofitService.getStations()) {
            is Result.Success -> {
                saveStations(result.data)
                errorRemoteLoading.postValue(null)
            }
            is Result.Error -> errorRemoteLoading.value = result
        }
    }


}
