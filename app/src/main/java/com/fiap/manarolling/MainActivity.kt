package com.fiap.manarolling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
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
import com.fiap.manarolling.ui.ChapterEditorScreen
import com.fiap.manarolling.ui.CharacterDetailScreen
import com.fiap.manarolling.ui.CharacterViewModel
import com.fiap.manarolling.ui.CreateCharacterScreen
import com.fiap.manarolling.ui.DiceScreen
import com.fiap.manarolling.ui.EditCharacterScreen
import com.fiap.manarolling.ui.ListCharactersScreen
import com.fiap.manarolling.ui.Routes
import com.fiap.manarolling.ui.StoryScreen
import com.fiap.manarolling.data.UserRole
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.ui.*
import com.fiap.manarolling.ui.theme.ManaRollingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nav = rememberNavController()
            val vm: CharacterViewModel = viewModel()
            val settings: SettingsViewModel = viewModel()

            val backStack = nav.currentBackStackEntryAsState()
            val current: NavDestination? = backStack.value?.destination
            val role by settings.role.collectAsState()


            ManaRollingAppTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val selectedCharacters =
                                when (role) {
                                    UserRole.PLAYER -> current?.route?.startsWith(Routes.DETAIL) == true ||
                                            current?.route?.startsWith(Routes.CREATE) == true ||
                                            current?.route?.startsWith(Routes.EDIT) == true ||
                                            current?.route?.startsWith(Routes.STORY) == true ||
                                            current?.route?.startsWith(Routes.CHAPTER_CREATE) == true ||
                                            current?.route?.startsWith(Routes.CHAPTER_EDIT) == true

                                    UserRole.MASTER -> current?.route?.startsWith(Routes.LIST) == true
                                    else -> false
                                }
                            NavigationBarItem(
                                selected = isCharactersRoute(),
                                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer),
                                onClick = {
                                    when (role) {
                                        UserRole.PLAYER -> nav.navigate(Routes.SPLASH) {
                                            launchSingleTop = true
                                        }

                                        UserRole.MASTER -> nav.navigate(Routes.LIST) {
                                            launchSingleTop = true
                                        }

                                        else -> nav.navigate(Routes.ROLE_SELECT) {
                                            launchSingleTop = true
                                        }
                                    }
                                },
                                icon = {
                                    if (role == UserRole.PLAYER) Icon(
                                        Icons.Filled.Person,
                                        null
                                    ) else Icon(Icons.Filled.Groups, null)
                                },
                                label = {
                                    Text(
                                        if (role == UserRole.PLAYER) "Meu Personagem" else "Personagens")
                                }
                            )
                            NavigationBarItem(
                                selected = current?.route == Routes.DICE,
                                onClick = { nav.navigate(Routes.DICE) { launchSingleTop = true } },
                                icon = { Icon(Icons.Filled.Casino, contentDescription = null) },
                                label = { Text("Dado") },
                                colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                        }
                    }
                ) { pad ->
                    // nav graph
                    NavHost(
                        navController = nav,
                        startDestination = Routes.SPLASH,
                        modifier = Modifier.padding(pad)
                    ) {
                        // decide destino (role + playerId)
                        composable(Routes.SPLASH) { SplashDecider(settings, vm, nav) }

                        // escolha de papel
                        composable(Routes.ROLE_SELECT) { RoleSelectScreen(settings, nav) }

                        // mestre (lista)
                        composable(Routes.LIST) { ListCharactersScreen(vm, nav) }

                        // criar personagem (usa callback pra salvar o id do jogador)
                        composable(Routes.CREATE) {
                            CreateCharacterScreen(
                                vm = vm,
                                nav = nav,
                                onCreated = { created: Character ->
                                    settings.setPlayerId(created.id)
                                    nav.navigate("${Routes.DETAIL}/${created.id}") {
                                        popUpTo(Routes.CREATE) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // detalhe
                        composable(
                            route = "${Routes.DETAIL}/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { back ->
                            val id = back.arguments?.getLong("id") ?: 0L
                            CharacterDetailScreen(vm, id, nav)
                        }

                        // editar
                        composable(
                            route = "${Routes.EDIT}/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { back ->
                            val id = back.arguments?.getLong("id") ?: 0L
                            EditCharacterScreen(vm, id, nav)
                        }

                        // história / capítulos
                        composable(
                            route = "${Routes.STORY}/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { back ->
                            val id = back.arguments?.getLong("id") ?: 0L
                            StoryScreen(vm, id, nav)
                        }
                        composable(
                            route = "${Routes.CHAPTER_CREATE}/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { back ->
                            val id = back.arguments?.getLong("id") ?: 0L
                            ChapterEditorScreen(vm, id, nav)
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
                            ChapterEditorScreen(vm, charId, nav, chapterId)
                        }

                        // dado
                        composable(Routes.DICE) { DiceScreen() }
                    }
                }
            }

        }
    }
}
