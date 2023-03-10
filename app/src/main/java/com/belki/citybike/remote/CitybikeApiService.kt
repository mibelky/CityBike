package com.belki.citybike.remote

import android.util.Log
import com.belki.citybike.dto.NetworkStation
import com.belki.citybike.local.StationDataSource
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.belki.citybike.dto.Result

object UriConstants {
    const val BASE_URL = "http://api.citybik.es/v2/networks/"
}

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(UriConstants.BASE_URL)
    .build()

interface CityBikeApiService {

    @GET("bubi")
    suspend fun getStations(@Query("fields") fields: String = "stations"): String
}

object CityBikeApi: StationDataSource {
    private val retrofitService: CityBikeApiService = retrofit.create(CityBikeApiService::class.java)

    override suspend fun getStations(): Result<List<NetworkStation>> {
        return try {
            val jsonStationsString = retrofitService.getStations()
            val jsonStationsObject = JSONObject(jsonStationsString)
            val stationsArrayList = jsonStationsObject.parseToStationsArray()
            Log.d("Stations", "Stations received and parsed")
            Log.d("Stations", "Stations size is ${stationsArrayList.size}")
            Result.Success(stationsArrayList)
        } catch (e: java.lang.Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    override suspend fun saveStations(stations: List<NetworkStation>) {
    }
}