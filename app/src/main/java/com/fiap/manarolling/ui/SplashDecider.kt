package com.fiap.manarolling.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.fiap.manarolling.data.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashDecider(
    settings: SettingsViewModel,
    vm: CharacterViewModel,
    nav: NavController
) {
    // coleta correta dos flows
    val role by settings.role.collectAsState()
    val playerId by settings.playerId.collectAsState()
    val chars by vm.characters.collectAsState()

    // tela neutra enquanto decide (usa pad pra remover o aviso)
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("ManaRolling") }) }
    ) { pad ->
        Box(
            modifier = Modifier
                .padding(pad)           // ← usa o content padding do Scaffold
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // decide destino
    LaunchedEffect(role, playerId, chars) {
        when (role) {
            null -> {
                nav.navigate(Routes.ROLE_SELECT) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                    launchSingleTop = true
                }
            }
            UserRole.MASTER -> {
                nav.navigate(Routes.LIST) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                    launchSingleTop = true
                }
            }
            UserRole.PLAYER -> {
                val id = playerId
                val ch = id?.let { vm.getCharacter(it) }
                if (ch != null) {
                    nav.navigate("${Routes.DETAIL}/${ch.id}") {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    nav.navigate(Routes.CREATE) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}
