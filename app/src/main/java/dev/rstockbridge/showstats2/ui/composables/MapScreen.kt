package dev.rstockbridge.showstats2.ui.composables

import android.os.Bundle
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import dev.rstockbridge.showstats2.MapViewModel
import dev.rstockbridge.showstats2.MapViewModelFactory
import dev.rstockbridge.showstats2.ProductionCoroutineContextProvider
import dev.rstockbridge.showstats2.R
import dev.rstockbridge.showstats2.api.SetlistfmApi
import dev.rstockbridge.showstats2.api.models.City
import kotlinx.coroutines.launch

@Composable
fun MapScreen(snackbarHostState: SnackbarHostState) {
    val viewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(ProductionCoroutineContextProvider(), SetlistfmApi)
    )

    val viewState by viewModel.viewState.collectAsState()

    val setlistfmId = "rstockbridge"

    LaunchedEffect(setlistfmId) {
        viewModel.fetchData(setlistfmId)
    }

    viewState.cities?.let {
        CityMapView(it)
    }

    if (viewState.networkCallInProgress) {
        ProgressBar()
    }

    viewState.userMessages.firstOrNull()?.let { userMessage ->
        LaunchedEffect(userMessage) {
            snackbarHostState.showSnackbar(userMessage.message)
            viewModel.userMessageShown(userMessage.uniqueId)
        }
    }
}

@Composable
private fun CityMapView(cities: Set<City>) {
    val mapView = rememberMapViewWithLifecycle()
    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels

    LaunchedEffect(mapView) {
        val googleMap = mapView.awaitMap()
        val builder = LatLngBounds.Builder()

        cities.forEach { city ->
            val latLng = LatLng(city.coordinates.latitude, city.coordinates.longitude)
            googleMap.addMarker { position(latLng) }
            builder.include(latLng)
        }

        val bounds = builder.build()
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                (0.1 * screenHeight).toInt()
            )
        )
        googleMap.uiSettings.isMapToolbarEnabled = false
    }

    AndroidView({ mapView })
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = getMapLifecycleObserver(mapView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

private fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
            Lifecycle.Event.ON_START -> mapView.onStart()
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            Lifecycle.Event.ON_STOP -> mapView.onStop()
            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
            else -> throw IllegalStateException()
        }
    }
