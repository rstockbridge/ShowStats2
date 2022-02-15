package dev.rstockbridge.showstats2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dagger.hilt.android.AndroidEntryPoint
import dev.rstockbridge.showstats2.ui.composables.main.MainScreen
import dev.rstockbridge.showstats2.ui.theme.ShowStats2Theme

@AndroidEntryPoint
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

sealed class TabScreen(
    val route: String,
    @StringRes val resourceId: Int,
    val iconResourceId: Int
) {
    object Map : TabScreen("map", R.string.map, R.drawable.ic_map)
    object Shows : TabScreen("shows", R.string.shows, R.drawable.ic_list)
}
