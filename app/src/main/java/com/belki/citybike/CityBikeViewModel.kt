package com.belki.citybike

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.*
import com.belki.citybike.domain.Station
import com.belki.citybike.dto.Result
import com.belki.citybike.dto.asStation
import com.belki.citybike.local.StationsLocalRepository
import com.belki.citybike.remote.CityBikeApi
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import kotlin.math.*

data class StationMarker(
    val latLng: LatLng,
    val id: String,
    val status: StationStatus
)

enum class StationStatus {
    ACTIVE, INACTIVE, OUT_OF_ORDER, SELECTED
}

class CityBikeViewModel(
    app: Application,
    private val repository: StationsLocalRepository
) : AndroidViewModel(app) {

    /*************************************************************************************
     *
     *                             INIT BLOCK
     *
     ************************************************************************************/
    init {
        viewModelScope.launch {
            repository.updateStations()
            if (repository.errorRemoteLoading.value == null) {
                when (val dbResult = repository.getStations()) {
                    is Result.Success -> {
                        stationList.value = dbResult.data.map { it.asStation() }
                        updateStationsDistance()
                        sortStationsByDistance()
                        _leaveLoadingEvent.postValue(true)
                    }
                    is Result.Error -> {
                        //TODO Handle Error case
                    }

                }
            }
        }
    }

    /*************************************************************************************
     *
     *           MAIN IN MEMORY DATA: STATION LIST, SELECTED STATION AND METHODS
     *
     ************************************************************************************/

    val stationList = MutableLiveData<List<Station>>()

    private val _selectedStation = MutableLiveData(Station("", 0, 0, 0.0, 0.0, "", 0))
    val selectedStation: LiveData<Station> = _selectedStation

    val selectedStationName =
        Transformations.map(selectedStation) { station -> station?.name ?: "n\\a" }

    val selectedStationFreeBikesString = Transformations.map(selectedStation) { station ->
        station?.freeBikes?.toString() ?: "n\\a"
    }

    val selectedStationEmptySlotsString = Transformations.map(selectedStation) { station ->
        station?.emptySlots?.toString() ?: "n\\a"
    }

    fun updateSelectedStation(id: String) = viewModelScope.launch {
        stationList.value?.find { it.id == id }?.let { _selectedStation.value = it }
    }

    fun updateStationsDistance() = viewModelScope.launch {
        stationList.value?.forEach { station ->
            station.distance = distanceBetween(
                station.latitude,
                station.longitude,
                location.value!!.latitude,
                location.value!!.longitude
            )
        }
    }

    fun sortStationsByDistance() = viewModelScope.launch {
        val sortedList = stationList.value?.sortedBy { station: Station -> station.distance }
        stationList.value = sortedList!!
    }

    /*************************************************************************************
     *
     *                              CUSTOM MY LOCATION LAYER
     *
     ************************************************************************************/

    private var _myLocationEnabled = true
    val myLocationEnabled: Boolean = _myLocationEnabled

    private val _location = MutableLiveData(budapestBridge)
    val location: LiveData<LatLng> = _location

    fun enableMyLocation() {
        _myLocationEnabled = true
    }

    fun disableMyLocation() {
        _myLocationEnabled = false
    }

    fun updateLocation(location: Location?) = location?.let {
        _location.value = LatLng(location.latitude, location.longitude)
    }

    /*************************************************************************************
     *
     *                              MARKERS LIVE DATA
     *
     ************************************************************************************/

    private val _selectedMarker = MutableLiveData<Marker?>(null)
    val selectedMarker: LiveData<Marker?> = _selectedMarker

    private val _myLocationMarker = MutableLiveData<Marker?>(null)
    val myLocationMarker: LiveData<Marker?> = _myLocationMarker

    val stationMarkerList = Transformations.map(stationList) { stationList ->
        stationList.map {
            StationMarker(
                LatLng(it.latitude, it.longitude),
                it.id,
                StationStatus.ACTIVE
            )
        }
    }

    fun updateSelectedMarker(value: Marker) = _selectedMarker.setValue(value)
    fun updateMyLocationMarker(value: Marker?) = _myLocationMarker.setValue(value)

    /*************************************************************************************
     *
     *                      NAVIGATION EVENTS AND METHODS
     *
     ************************************************************************************/

    private val _startARideEvent = MutableLiveData(false)
    val startARideEvent: LiveData<Boolean> = _startARideEvent

    private val _leaveQrEvent = MutableLiveData(false)
    val leaveQrEvent: LiveData<Boolean> = _leaveQrEvent

    private val _leaveLoadingEvent = MutableLiveData(false)
    val leaveLoadingEvent: LiveData<Boolean> = _leaveLoadingEvent

    fun startStartARideEvent() = _startARideEvent.setValue(true)
    fun finishStartARideEvent() = _startARideEvent.setValue(false)
    fun startLeaveQrEvent() = _leaveQrEvent.setValue(true)
    fun finishLeaveQrEvent() = _leaveQrEvent.setValue(false)
    fun startLeaveLoadingEvent() = _leaveLoadingEvent.setValue(true)
    fun finishLeaveLoadingEvent() = _leaveLoadingEvent.setValue(false)

    /*************************************************************************************
     *
     *                             BANNER LIVE DATA AND METHODS
     *
     ************************************************************************************/

    private val _isBannerVisible = MutableLiveData(false)
    val isBannerVisible: LiveData<Boolean> = _isBannerVisible

    private val _bannerMessage = MutableLiveData<String?>(null)
    val bannerMessage: LiveData<String?> = _bannerMessage

    private val _bannerButtonText = MutableLiveData<String?>(null)
    val bannerButtonText: LiveData<String?> = _bannerButtonText

    private val _bannerButtonOnClick = MutableLiveData<(() -> Unit)?>(null)

    fun bannerButtonClicked() {
        _bannerButtonOnClick.value?.invoke()
    }

    fun showBanner(message: String, buttonText: String, buttonOnClick: (() -> Unit)?) {
        _bannerMessage.value = message
        _bannerButtonText.value = buttonText
        _bannerButtonOnClick.value = { buttonOnClick?.invoke() }
        _isBannerVisible.value = true
    }

    fun hideBanner() {
        _isBannerVisible.value = false
        _bannerMessage.value = null
        _bannerButtonText.value = null
        _bannerButtonOnClick.value = null
    }

    /*************************************************************************************
     *
     *                    BOTTOM SHEET LIVE DATA AND METHODS
     *
     ************************************************************************************/

    private val _bottomSheetBehavior = MutableLiveData(BottomSheetBehavior.STATE_HIDDEN)
    val bottomSheetBehavior: LiveData<Int> = _bottomSheetBehavior

    fun expandBottomSheet() = _bottomSheetBehavior.setValue(BottomSheetBehavior.STATE_EXPANDED)

    fun hideBottomSheet() = _bottomSheetBehavior.setValue(BottomSheetBehavior.STATE_HIDDEN)

    /*************************************************************************************
     *
     *                    EDGE-TO-EDGE AND IMMERSIVE BEHAVIOR
     *
     ************************************************************************************/

    private val _systemBarsStateHidden = MutableLiveData(false)
    val systemBarState: LiveData<Boolean> = _systemBarsStateHidden

    /*************************************************************************************
     *
     *                            CONSTANTS AND SOME MATH
     *
     ************************************************************************************/

    companion object {
        val budapestBridge = LatLng(47.490930, 19.049129)

        const val budapestEarthRadius = 6366661
        fun distanceBetween(latA: Double, lngA: Double, latB: Double, lngB: Double): Int {
            //Convert degrees to radians
            val radLatA = latA * PI / 180
            val radLatB = latB * PI / 180
            val deltaLat = (latA - latB) * PI / 180
            val deltaLng = (lngA - lngB) * PI / 180

            //Haversine formula
            val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                    cos(radLatA) * cos(radLatB) * sin(deltaLng / 2) * sin(deltaLng / 2)

            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            val d2 = budapestEarthRadius * c; // in metres

            return d2.roundToInt()
        }
    }
}


