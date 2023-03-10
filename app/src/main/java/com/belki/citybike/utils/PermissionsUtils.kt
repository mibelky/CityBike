package com.belki.citybike.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun Fragment.foregroundLocationPermissionApproved() =
    PackageManager.PERMISSION_GRANTED ==
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )

fun Fragment.cameraPermissionApproved() =
    PackageManager.PERMISSION_GRANTED ==
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            )

@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.awaitCurrentLocation(priority: Int): Location? {
    return suspendCancellableCoroutine {
        // to use for request cancellation upon coroutine cancellation
        val cts = CancellationTokenSource()
        getCurrentLocation(priority, cts.token)
            .addOnSuccessListener { location ->
                // remember location is nullable, this happens sometimes
                // when the request expires before an update is acquired
                it.resume(location)
            }
            .addOnFailureListener { e ->
                it.resumeWithException(e)
            }
        it.invokeOnCancellation {
            cts.cancel()
        }
    }
}
