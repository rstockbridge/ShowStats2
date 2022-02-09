package dev.rstockbridge.showstats2.ui.composables

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import dev.rstockbridge.showstats2.MapViewModel
import dev.rstockbridge.showstats2.MapViewModelFactory
import dev.rstockbridge.showstats2.ProductionCoroutineContextProvider
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
        GoogleMapView(it)
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
private fun GoogleMapView(cities: List<City>) {

    val initialCameraPosition = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 1f)

    val cameraPositionState = rememberCameraPositionState {
        position = initialCameraPosition
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false,
                mapToolbarEnabled = false,
                zoomControlsEnabled = false
            )
        )
    }

    val coroutineScope = rememberCoroutineScope()

    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels

    GoogleMap(
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        onMapLoaded = {
            coroutineScope.launch {
                val builder = LatLngBounds.Builder()

                cities.forEach { city ->
                    val latLng = LatLng(city.coordinates.latitude, city.coordinates.longitude)
                    builder.include(latLng)
                }

                val bounds = builder.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        (0.1 * screenHeight).toInt()
                    )
                )
            }
        },
        googleMapOptionsFactory = {
            GoogleMapOptions().camera(initialCameraPosition)
        }
    ) {
        cities.forEach { city ->
            Marker(
                position = LatLng(city.coordinates.latitude, city.coordinates.longitude),
                title = city.name,
            )
        }
    }
}
