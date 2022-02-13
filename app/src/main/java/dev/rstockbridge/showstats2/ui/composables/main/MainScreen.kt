package dev.rstockbridge.showstats2.ui.composables.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.rstockbridge.showstats2.TabScreen
import dev.rstockbridge.showstats2.api.models.City
import dev.rstockbridge.showstats2.api.models.Show
import dev.rstockbridge.showstats2.ui.composables.fetchdata.FetchDataScreen
import dev.rstockbridge.showstats2.ui.composables.map.MapScreen
import dev.rstockbridge.showstats2.ui.composables.shows.ShowsScreen

@Composable
fun MainScreen(
    licensesOnClick: () -> Unit
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()

    var showList by rememberSaveable { mutableStateOf(emptyList<Show>()) }

    val switchUserIdOnClick: () -> Unit = {
        navController.navigate("FetchDataScreen") {
            navController.backQueue.clear()
        }
    }

    val bottomNavOnClick: (TabScreen) -> Unit = { screen ->
        navController.navigate(screen.route) {
            popUpTo(navController.currentDestination!!.id) {
                inclusive = true
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val onDataLoaded: (List<Show>) -> Unit = { shows ->
        showList = shows
        navController.navigate(TabScreen.Map.route) {
            popUpTo("FetchDataScreen") { inclusive = true }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val screenSelected: (TabScreen) -> Boolean = { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    val barState = rememberSaveable { (mutableStateOf(true)) }

    Scaffold(
        modifier = Modifier,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                switchUserIdOnClick = switchUserIdOnClick,
                licensesOnClick = licensesOnClick,
                topAppBarState = barState
            )
        },
        bottomBar = {
            BottomNavBar(
                tabScreens = listOf(
                    TabScreen.Map,
                    TabScreen.Shows,
                ),
                screenSelected = screenSelected,
                bottomNavOnClick = bottomNavOnClick,
                bottomNavBarState = barState
            )
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = "FetchDataScreen",
            Modifier.padding(innerPadding)
        ) {
            composable("FetchDataScreen") {
                LaunchedEffect(Unit) {
                    barState.value = false
                }
                FetchDataScreen(
                    scaffoldState.snackbarHostState,
                    onDataLoaded
                )
            }
            composable(TabScreen.Map.route) {
                LaunchedEffect(Unit) {
                    barState.value = true
                }
                MapScreen(getCitiesFromShows(showList))
            }
            composable(TabScreen.Shows.route) {
                LaunchedEffect(Unit) {
                    barState.value = true
                }
                ShowsScreen(showList)
            }
        }
    }
}

private fun getCitiesFromShows(showList: List<Show>): List<City> {
    val cities = mutableSetOf<City>()
    showList.forEach { show ->
        cities.add(show.venue.city)
    }

    return cities.toList()
}

