package com.fiap.manarolling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
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
import com.fiap.manarolling.ui.EditCharacterScreen
import com.fiap.manarolling.ui.ListCharactersScreen
import com.fiap.manarolling.ui.Routes
import com.fiap.manarolling.ui.StoryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nav = rememberNavController()
            val vm: CharacterViewModel = viewModel()

            val backStack = nav.currentBackStackEntryAsState()
            val current: NavDestination? = backStack.value?.destination
            fun isCharactersRoute(): Boolean =
                current?.route?.startsWith(Routes.LIST) == true ||
                        current?.route?.startsWith(Routes.CREATE) == true ||
                        current?.route?.startsWith(Routes.DETAIL) == true ||
                        current?.route?.startsWith(Routes.EDIT) == true ||
                        current?.route?.startsWith(Routes.STORY) == true ||
                        current?.route?.startsWith(Routes.CHAPTER_CREATE) == true ||
                        current?.route?.startsWith(Routes.CHAPTER_EDIT) == true

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = isCharactersRoute(),
                            onClick = { nav.navigate(Routes.LIST) { launchSingleTop = true } },
                            icon = { Icon(Icons.Filled.Groups, contentDescription = null) },
                            label = { Text("Personagens") }
                        )
                        NavigationBarItem(
                            selected = current?.route == Routes.DICE,
                            onClick = { nav.navigate(Routes.DICE) { launchSingleTop = true } },
                            icon = { Icon(Icons.Filled.Casino, contentDescription = null) },
                            label = { Text("Dado") }
                        )
                    }
                }
            ) { pad ->
                // nav graph
                NavHost(
                    navController = nav,
                    startDestination = Routes.LIST,
                    modifier = Modifier.padding(pad)
                ) {
                    composable(Routes.LIST) { ListCharactersScreen(vm, nav) }
                    composable(Routes.CREATE) { CreateCharacterScreen(vm, nav) }
                    composable(
                        route = "${Routes.DETAIL}/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStack ->
                        val id = backStack.arguments?.getLong("id") ?: 0L
                        CharacterDetailScreen(vm, id, nav)
                    }
                    composable(
                        route = "${Routes.EDIT}/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStack ->
                        val id = backStack.arguments?.getLong("id") ?: 0L
                        EditCharacterScreen(vm, id, nav)
                    }
                    composable(
                        route = "${Routes.STORY}/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStack ->
                        val id = backStack.arguments?.getLong("id") ?: 0L
                        StoryScreen(vm, id, nav)
                    }
                    composable(
                        route = "${Routes.CHAPTER_CREATE}/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStack ->
                        val id = backStack.arguments?.getLong("id") ?: 0L
                        ChapterEditorScreen(vm, id, nav)
                    }
                    composable(
                        route = "${Routes.CHAPTER_EDIT}/{charId}/{chapterId}",
                        arguments = listOf(
                            navArgument("charId") { type = NavType.LongType },
                            navArgument("chapterId") { type = NavType.LongType }
                        )
                    ) { backStack ->
                        val charId = backStack.arguments?.getLong("charId") ?: 0L
                        val chapterId = backStack.arguments?.getLong("chapterId") ?: 0L
                        ChapterEditorScreen(vm, charId, nav, chapterId)
                    }


                    composable(Routes.DICE) { DiceScreen() }
                }
            }
        }
    }
}
