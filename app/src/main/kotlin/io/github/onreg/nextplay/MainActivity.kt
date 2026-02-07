package io.github.onreg.nextplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import io.github.onreg.core.ui.theme.NextPlayTheme
import io.github.onreg.feature.game.details.impl.pane.GameDetailsPane
import io.github.onreg.feature.game.list.impl.pane.GamesPane

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
                        startDestination = AppRoutes.GAMES,
                        modifier = Modifier.padding(paddingValues),
                    ) {
                        composable(AppRoutes.GAMES) {
                            GamesPane(
                                onOpenGameDetails = { gameId ->
                                    nav.navigate(AppRoutes.gameDetailsRoute(gameId))
                                },
                            )
                        }

                        composable(
                            route = AppRoutes.GAME_DETAILS,
                            arguments = listOf(
                                navArgument(AppRoutes.ARG_GAME_ID) { type = NavType.StringType },
                            ),
                        ) { backStackEntry ->
                            val gameId = backStackEntry.arguments
                                ?.getString(AppRoutes.ARG_GAME_ID)
                                ?.toIntOrNull()
                                ?: return@composable
                            GameDetailsPane(
                                gameId = gameId,
                                onGoBack = { nav.popBackStack() },
                                onOpenGameDetails = { nextGameId ->
                                    nav.navigate(AppRoutes.gameDetailsRoute(nextGameId.toString()))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private object AppRoutes {
    const val GAMES: String = "GamesPane"
    const val ARG_GAME_ID: String = "gameId"
    const val GAME_DETAILS: String = "GameDetails/{$ARG_GAME_ID}"

    fun gameDetailsRoute(gameId: String): String = "GameDetails/$gameId"
}
