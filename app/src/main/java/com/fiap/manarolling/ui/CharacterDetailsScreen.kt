package com.fiap.manarolling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fiap.manarolling.R
import com.fiap.manarolling.model.Character

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    vm: CharacterViewModel,
    id: Long,
    nav: NavController
) {
    val c: Character? = vm.getCharacter(id)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personagem") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (c != null) {
                        IconButton(onClick = { nav.navigate("${Routes.EDIT}/$id") }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = {
                            vm.deleteCharacter(c.id)
                            nav.popBackStack()
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate("${Routes.STORY}/$id") },
                text = { Text("Ver história") },
                icon = { Icon(Icons.Filled.Edit, contentDescription = null) }
            )
        }
    ) { pad ->
        if (c == null) {
            Box(
                Modifier.padding(pad).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Personagem não encontrado") }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Capa: borda a borda
                item {
                    AsyncImage(
                        model = c.photoUri ?: R.drawable.default_character,
                        contentDescription = "Foto do personagem",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }

                // Nome
                item {
                    Text(
                        c.name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }


                item {
                    ElevatedCard(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Região: ${c.region}")
                            Text("Idade: ${c.age}")
                            Text("Classe: ${c.clazz}")
                            Text("Nível: ${c.level}")
                        }
                    }
                }

                // Atributos
                item {
                    Text(
                        "Atributos",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                items(
                    listOf(
                        "Inteligência" to c.attributes.intelligence,
                        "Destreza"     to c.attributes.dexterity,
                        "Força"        to c.attributes.strength,
                        "Agilidade"    to c.attributes.agility,
                        "Carisma"      to c.attributes.charisma
                    )
                ) { (label, value) ->
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        AttributeStat(label, value)
                    }
                }


                item {
                    Text(
                        "Pontos restantes: ${c.availablePoints}",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AttributeStat(label: String, value: Int) {
    fun bar(v: Int) = (v.coerceIn(0, 50)) / 50f
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            LinearProgressIndicator(progress = { bar(value) }, modifier = Modifier.fillMaxWidth())
            Text("$value / 50", style = MaterialTheme.typography.bodySmall)
        }
    }
}
