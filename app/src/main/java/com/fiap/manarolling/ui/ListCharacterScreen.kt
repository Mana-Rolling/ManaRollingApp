package com.fiap.manarolling.ui

import coil.compose.AsyncImage
import com.fiap.manarolling.R
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

val pixelifySansFamily = FontFamily(
    Font(R.font.pixelify_sans_medium, FontWeight.Medium)
    // Adicione outras variações de peso/estilo se necessário
)

val backgroundColor = Color(0xFF0D0D0D)
val primaryTextColor = Color.Black
val hintColor = Color(0xFFAAAAAA)
val accentColor = Color(0xFFA78BFA)
val cardButtonBgColor = Color(0xFF2D2D2D)
val bottomMenuBgColor = Color(0xFF1A1A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListCharactersScreen(vm: CharacterViewModel, nav: NavController) {
    val list = vm.characters.collectAsState().value

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { nav.navigate(Routes.CREATE) }) {
                Icon(Icons.Filled.Add, contentDescription = "Criar")
            }
        },
    ) { pad ->
        if (list.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Groups, contentDescription = null)
                    Spacer(Modifier.height(8.dp)); Text("Nenhum personagem",
                    style = MaterialTheme.typography.titleMedium)
                    Text("Toque no + para criar", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(Modifier.padding(pad).padding(12.dp)) {
                items(list) { c ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clickable { nav.navigate("${Routes.DETAIL}/${c.id}") }
                    ) {
                        Row(
                            Modifier.fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // Thumb redonda
                            if (c.photoUri != null) {
                                AsyncImage(
                                    model = c.photoUri ?: R.drawable.default_character,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(56.dp).clip(CircleShape),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(
                                        Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Person, contentDescription = null)
                                    }
                                }
                            }

                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    c.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "Classe: ${c.clazz.ifBlank { "-" }} • Nível ${c.level}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
