package dev.rstockbridge.showstats2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dev.rstockbridge.showstats2.api.models.City
import dev.rstockbridge.showstats2.api.models.Show
import dev.rstockbridge.showstats2.ui.composables.ShowsScreen
import dev.rstockbridge.showstats2.ui.composables.MapScreen
import dev.rstockbridge.showstats2.ui.composables.FetchDataScreen
import dev.rstockbridge.showstats2.ui.theme.DarkerGreen
import dev.rstockbridge.showstats2.ui.theme.ShowStats2Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val licensesOnClick: () -> Unit = {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }

        setContent {
            this.window.statusBarColor = ContextCompat.getColor(this, R.color.darker_green)

            ShowStats2Theme {
                MainScreen(
                    licensesOnClick,
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    licensesOnClick: () -> Unit
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()

    var showList by remember { mutableStateOf(emptyList<Show>()) }

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

@Composable
fun TopAppBar(
    switchUserIdOnClick: () -> Unit,
    licensesOnClick: () -> Unit,
    topAppBarState: MutableState<Boolean>
) {
    var showMenu by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    AnimatedVisibility(
        visible = topAppBarState.value,
        enter = slideInVertically(initialOffsetY = { it }),
        content = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(
                        onClick = { showMenu = !showMenu }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                showMenu = false
                                switchUserIdOnClick()
                            }
                        ) {
                            Text(stringResource(R.string.switch_user_id))
                        }
                        DropdownMenuItem(
                            onClick = licensesOnClick
                        ) {
                            Text(stringResource(R.string.licenses))
                        }
                        DropdownMenuItem(
                            onClick = {
                                uriHandler.openUri("https://github.com/rstockbridge/ShowStats/blob/main/playstore/privacy_policy.md")
                            }
                        ) {
                            Text(stringResource(R.string.privacy_policy))
                        }
                    }
                }
            )
        }
    )
}

@Composable
fun BottomNavBar(
    tabScreens: List<TabScreen>,
    screenSelected: (TabScreen) -> Boolean,
    bottomNavOnClick: (TabScreen) -> Unit,
    bottomNavBarState: MutableState<Boolean>
) {
    AnimatedVisibility(
        visible = bottomNavBarState.value,
        enter = slideInVertically(initialOffsetY = { it }),
        content = {
            BottomNavigation {
                tabScreens.forEach { screen ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                painterResource(id = screen.iconResourceId),
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = screenSelected(screen),
                        onClick = { bottomNavOnClick(screen) }
                    )
                }
            }
        }
    )
}

sealed class TabScreen(
    val route: String,
    @StringRes val resourceId: Int,
    val iconResourceId: Int
) {
    object Map : TabScreen("map", R.string.map, R.drawable.ic_map)
    object Shows : TabScreen("shows", R.string.shows, R.drawable.ic_list)
}

private fun getCitiesFromShows(showList: List<Show>): List<City> {
    val cities = mutableSetOf<City>()
    showList.forEach { show ->
        cities.add(show.venue.city)
    }

    return cities.toList()
}
