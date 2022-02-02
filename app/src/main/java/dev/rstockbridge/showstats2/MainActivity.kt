package dev.rstockbridge.showstats2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.rstockbridge.showstats2.ui.composables.ListOfShowsScreen
import dev.rstockbridge.showstats2.ui.composables.MapScreen
import dev.rstockbridge.showstats2.ui.theme.ShowStats2Theme

class MainActivity : ComponentActivity() {

    private val screens = listOf(
        TabScreen.Map,
        TabScreen.Shows,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShowStats2Theme {
                TabbedScreen(screens)
            }
        }
    }
}

@Composable
fun TabbedScreen(
    tabScreens: List<TabScreen>
) {
    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        modifier = Modifier,
        scaffoldState = scaffoldState,
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                tabScreens.forEach { screen ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                painterResource(id = screen.iconResourceId),
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = TabScreen.Map.route,
            Modifier.padding(innerPadding)
        ) {
            composable(TabScreen.Map.route) { MapScreen(scaffoldState.snackbarHostState) }
            composable(TabScreen.Shows.route) { ListOfShowsScreen(scaffoldState.snackbarHostState) }
        }
    }
}

sealed class TabScreen(
    val route: String,
    @StringRes val resourceId: Int,
    val iconResourceId: Int
) {
    object Map : TabScreen("map", R.string.map, R.drawable.ic_map)
    object Shows : TabScreen("shows", R.string.shows, R.drawable.ic_list)
}
