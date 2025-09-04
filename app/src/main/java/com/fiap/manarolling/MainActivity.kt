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
import com.fiap.manarolling.ui.CharacterDetailScreen
import com.fiap.manarolling.ui.CharacterViewModel
import com.fiap.manarolling.ui.CreateCharacterScreen
import com.fiap.manarolling.ui.ListCharactersScreen
import com.fiap.manarolling.ui.Routes

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val nav = rememberNavController()
            val vm: CharacterViewModel = viewModel() // AndroidViewModel: usa DefaultFactory

            Surface(color = MaterialTheme.colorScheme.background) {
                NavHost(navController = nav, startDestination = Routes.LIST) {
                    composable(Routes.LIST) { ListCharactersScreen(vm, nav) }
                    composable(Routes.CREATE) { CreateCharacterScreen(vm, nav) }
                    composable(
                        route = "${Routes.DETAIL}/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType })
                    ) { backStack ->
                        val id = backStack.arguments?.getLong("id") ?: 0L
                        CharacterDetailScreen(vm, id, nav)
                    }
                }
            }
        }
    }
}
