package dev.rstockbridge.showstats2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.rstockbridge.showstats2.api.SetlistfmApi
import dev.rstockbridge.showstats2.ui.composables.ListOfShowsUi
import dev.rstockbridge.showstats2.ui.theme.ShowStats2Theme

class MainActivity : ComponentActivity() {

    private val viewModelFactory =
        ListOfShowsViewModelFactory(ProductionCoroutineContextProvider(), SetlistfmApi)
    private val viewModel by viewModels<ListOfShowsViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ShowStats2Theme {
                Screen(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }
}

@Composable
fun Screen(
    viewModel: ListOfShowsViewModel,
) {
    val viewState by viewModel.viewState.collectAsState()
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        modifier = Modifier,
        scaffoldState = scaffoldState
    ) {
        ListOfShowsUi(viewState)

        viewState.userMessages.firstOrNull()?.let { userMessage ->
            LaunchedEffect(userMessage) {
                scaffoldState.snackbarHostState.showSnackbar(userMessage.message)
                viewModel.userMessageShown(userMessage.uniqueId)
            }
        }
    }
}
