package com.fiap.manarolling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fiap.manarolling.data.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectScreen(settings: SettingsViewModel, nav: NavController) {
    Scaffold(topBar = { TopAppBar(title = { Text("Como quer jogar?") }) }) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ElevatedButton(
                onClick = {
                    settings.pickRole(UserRole.PLAYER)
                    nav.navigate(Routes.CREATE) { popUpTo(Routes.ROLE_SELECT) { inclusive = true } }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Filled.Person, null); Spacer(Modifier.width(8.dp)); Text("Sou Jogador")
            }
            OutlinedButton(
                onClick = {
                    settings.pickRole(UserRole.MASTER)
                    nav.navigate(Routes.LIST) { popUpTo(Routes.ROLE_SELECT) { inclusive = true } }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Filled.Groups, null); Spacer(Modifier.width(8.dp)); Text("Sou Mestre")
            }
            Text("Você pode mudar depois limpando os dados do app.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
