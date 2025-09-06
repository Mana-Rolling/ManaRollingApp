package com.fiap.manarolling.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fiap.manarolling.R
import com.fiap.manarolling.data.UserRole
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val pixelifySansFamily = FontFamily(
    Font(R.font.pixelify_sans_medium, FontWeight.Medium)
    // Adicione outras variações de peso/estilo se necessário
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectScreen(settings: SettingsViewModel, nav: NavController) {
    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(70.dp)// Altura do ConstraintLayout - (2 * padding de 16dp) / 2 para alinhar = 89 - 32 = 57
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mr_logo),
                        contentDescription = "Logo Mana Rolling",
                        modifier = Modifier.size(59.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primaryContainer)
                    )
                    Text(
                        text = "Mana Rolling",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        fontSize = 20.sp,
                        fontFamily = pixelifySansFamily,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
//                Spacer(modifier = Modifier.padding(16.dp))
                Text(
                    modifier = Modifier.padding(16.dp,20.dp),
                    text = "Como quer jogar?",
                    fontSize = 20.sp,
                )
            }
        }
    ) { pad ->
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
