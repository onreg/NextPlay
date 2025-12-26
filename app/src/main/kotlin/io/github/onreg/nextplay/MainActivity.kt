package io.github.onreg.nextplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.feature.game.impl.pane.GameDetailsPane
import io.github.onreg.feature.game.impl.pane.GamesPane
import io.github.onreg.feature.game.impl.pane.GamesRoute

@AndroidEntryPoint
internal class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NextPlayTheme {
                Scaffold { paddingValues ->
                    val nav = rememberNavController()
                    NavHost(
                        navController = nav,
                        startDestination = GamesRoute.games,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(GamesRoute.games) {
                            GamesPane(navController = nav)
                        }
                        composable(GamesRoute.details) { backStackEntry ->
                            GameDetailsPane(
                                gameId = backStackEntry.arguments?.getString("gameId").orEmpty()
                            )
                        }
                    }
                }
            }
        }
    }
}
