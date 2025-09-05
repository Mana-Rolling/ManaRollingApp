package com.fiap.manarolling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.fiap.manarolling.ui.theme.ManaRollingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nav = rememberNavController()
            val vm: CharacterViewModel = viewModel() // AndroidViewModel via DefaultFactory

            ManaRollingAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = nav, startDestination = Routes.LIST) {
                        // Lista de personagens
                        composable(Routes.LIST) {
                            ListCharactersScreen(vm, nav)
                        }

                        // Criar personagem
                        composable(Routes.CREATE) {
                            CreateCharacterScreen(vm, nav)
                        }

                        // Detalhe do personagem
                        composable(
                            route = "${Routes.DETAIL}/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { backStack ->
                            val id = backStack.arguments?.getLong("id") ?: 0L
                            CharacterDetailScreen(vm, id, nav)
                        }

                        // Editar personagem
                        composable(
                            route = "${Routes.EDIT}/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { backStack ->
                            val id = backStack.arguments?.getLong("id") ?: 0L
                            EditCharacterScreen(vm, id, nav)
                        }

                        // Lista da história (capítulos) do personagem
                        composable(
                            route = "${Routes.STORY}/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { backStack ->
                            val id = backStack.arguments?.getLong("id") ?: 0L
                            StoryScreen(vm, id, nav)
                        }

                        // Criar capítulo
                        composable(
                            route = "${Routes.CHAPTER_CREATE}/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.LongType })
                        ) { backStack ->
                            val id = backStack.arguments?.getLong("id") ?: 0L
                            ChapterEditorScreen(vm, id, nav)
                        }

                        // Editar capítulo
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
                    }
                }
            }

        }
    }
}
