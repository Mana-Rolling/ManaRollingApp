package com.fiap.manarolling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.multiplayer.MultiplayerViewModel
import com.fiap.manarolling.ui.*
import com.fiap.manarolling.ui.character.CharacterViewModel
import com.fiap.manarolling.ui.character.ListCharactersScreen
import com.fiap.manarolling.ui.multiplayer.LobbyScreen
import com.fiap.manarolling.ui.multiplayer.MultiplayerScreen
import com.fiap.manarolling.ui.theme.ManaRollingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nav = rememberNavController()

            // VMs no escopo da Activity
            val characterVm: CharacterViewModel = viewModel()
            val settingsVm: SettingsViewModel = viewModel()
            val mpVm: MultiplayerViewModel = viewModel()

            val backStack = nav.currentBackStackEntryAsState()
            val current: NavDestination? = backStack.value?.destination
            val currentRoute = current?.route ?: ""

            val selectedCharacters = currentRoute.startsWith(Routes.LIST) ||
                    currentRoute.startsWith("${Routes.DETAIL}/") ||
                    currentRoute.startsWith(Routes.CREATE) ||
                    currentRoute.startsWith("${Routes.EDIT}/") ||
                    currentRoute.startsWith("${Routes.STORY}/") ||
                    currentRoute.startsWith("${Routes.CHAPTER_CREATE}/") ||
                    currentRoute.startsWith("${Routes.CHAPTER_EDIT}/")

            ManaRollingAppTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedCharacters,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                onClick = { nav.navigate(Routes.LIST) { launchSingleTop = true } },
                                icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                label = { Text("Personagens") }
                            )
                            NavigationBarItem(
                                selected = currentRoute.startsWith(Routes.LOBBY) ||
                                        currentRoute.startsWith(Routes.MULTIPLAYER) ||
                                        currentRoute.startsWith(Routes.SESSION_CHAR_DETAILS),
                                onClick = { nav.navigate(Routes.LOBBY) { launchSingleTop = true } },
                                icon = { Icon(Icons.Filled.Groups, contentDescription = null) },
                                label = { Text("Multiplayer") },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                            NavigationBarItem(
                                selected = currentRoute.startsWith(Routes.DICE),
                                onClick = { nav.navigate(Routes.DICE) { launchSingleTop = true } },
                                icon = { Icon(Icons.Filled.Casino, contentDescription = null) },
                                label = { Text("Dado") },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                ) { pad ->
                    NavHost(
                        navController = nav,
                        startDestination = Routes.LIST,
                        modifier = Modifier.padding(pad)
                    ) {
                        // Lista (nome correto: ListCharacterScreen)
                        composable(Routes.LIST) { ListCharactersScreen(characterVm, nav) }

                        // Criar personagem (assinatura correta da sua tela)
                        composable(Routes.CREATE) {
                            CreateCharacterScreen(
                                onBack = { nav.popBackStack() },
                                onCreated = { created: Character ->
                                    nav.navigate("${Routes.DETAIL}/${created.id}") {
                                        popUpTo(Routes.CREATE) { inclusive = true }
                                    }
                                },
                                vm = settingsVm,
                                repoVM = characterVm
                            )
                        }

                        //
                        composable(
                            route = "${Routes.DETAIL}/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { back ->
                            val id = back.arguments?.getLong("id") ?: 0L
                            CharacterDetailScreen(characterVm, id, nav)
                        }

                        // Editar
                        composable(
                            route = "${Routes.EDIT}/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { back ->
                            val id = back.arguments?.getLong("id") ?: 0L
                            EditCharacterScreen(characterVm, id, nav)
                        }

                        // Offline (se usar)
                        composable(Routes.OFFLINE) {
                            OfflineCharactersScreen(onBack = { nav.popBackStack() })
                        }

                        // História/Capítulos
                        composable(
                            route = "${Routes.STORY}/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { back ->
                            val id = back.arguments?.getLong("id") ?: 0L
                            StoryScreen(characterVm, id, nav)
                        }
                        composable(
                            route = "${Routes.CHAPTER_CREATE}/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { back ->
                            val id = back.arguments?.getLong("id") ?: 0L
                            ChapterEditorScreen(characterVm, id, nav)
                        }
                        composable(
                            route = "${Routes.CHAPTER_EDIT}/{charId}/{chapterId}",
                            arguments = listOf(
                                navArgument("charId") { type = NavType.LongType },
                                navArgument("chapterId") { type = NavType.LongType }
                            )
                        ) { back ->
                            val charId = back.arguments?.getLong("charId") ?: 0L
                            val chapterId = back.arguments?.getLong("chapterId") ?: 0L
                            ChapterEditorScreen(characterVm, charId, nav, chapterId)
                        }

                        // Dado
                        composable(Routes.DICE) { DiceScreen() }

                        // Lobby
                        composable(Routes.LOBBY) {
                            LobbyScreen(
                                vm = mpVm,
                                characterVm = characterVm,
                                navToSession = { sessionId ->
                                    nav.navigate("${Routes.MULTIPLAYER}/$sessionId")
                                }
                            )
                        }

                        // Sessão
                        composable("${Routes.MULTIPLAYER}/{sessionId}") { back ->
                            val sessionId = back.arguments?.getString("sessionId") ?: return@composable
                            MultiplayerScreen(
                                sessionId = sessionId,
                                onLeave = { nav.popBackStack() },
                                onOpenCharacter = { ownerUid, charId ->
                                    nav.navigate("${Routes.SESSION_CHAR_DETAILS}/$sessionId/$ownerUid/$charId")
                                },
                                onCreateCharacter = { nav.navigate(Routes.CREATE) },
                                vm = mpVm,
                                characterVm = characterVm
                            )
                        }

                        // Ficha online
                        composable("${Routes.SESSION_CHAR_DETAILS}/{sessionId}/{ownerUid}/{charId}") { back ->
                            val sessionId = back.arguments?.getString("sessionId") ?: return@composable
                            val ownerUid  = back.arguments?.getString("ownerUid") ?: return@composable
                            val charId    = back.arguments?.getString("charId")?.toLongOrNull() ?: return@composable
                            SessionCharacterDetailsScreen(
                                sessionId = sessionId,
                                ownerUid = ownerUid,
                                charId = charId,
                                nav = nav,
                                vm = mpVm
                            )
                        }
                    }
                }
            }
        }
    }
}
