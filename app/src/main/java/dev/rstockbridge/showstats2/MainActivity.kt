package dev.rstockbridge.showstats2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler

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

    fun bottomNavOnClick(screen: TabScreen) = navController.navigate(screen.route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    fun screenSelected(screen: TabScreen): Boolean =
        currentDestination?.hierarchy?.any { it.route == screen.route } == true

    Scaffold(
        modifier = Modifier,
        scaffoldState = scaffoldState,
        topBar = { TopAppBar() },
        bottomBar = {
            BottomNavBar(
                tabScreens = tabScreens,
                screenSelected = ::screenSelected,
                bottomNavOnClick = ::bottomNavOnClick
            )
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

@Composable
fun TopAppBar() {
    var showMenu by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

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
                    onClick = { /*TODO*/ }
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

@Composable
fun BottomNavBar(
    tabScreens: List<TabScreen>,
    screenSelected: (TabScreen) -> Boolean,
    bottomNavOnClick: (TabScreen) -> Unit
) {
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

sealed class TabScreen(
    val route: String,
    @StringRes val resourceId: Int,
    val iconResourceId: Int
) {
    object Map : TabScreen("map", R.string.map, R.drawable.ic_map)
    object Shows : TabScreen("shows", R.string.shows, R.drawable.ic_list)
}
