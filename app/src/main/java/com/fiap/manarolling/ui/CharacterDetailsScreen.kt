package com.fiap.manarolling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fiap.manarolling.model.Character

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(vm: CharacterViewModel, id: Long, nav: NavController) {
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
                            Icon(Icons.Filled.Edit, contentDescription = "Editar personagem")
                        }
                        IconButton(onClick = { vm.deleteCharacter(c.id); nav.popBackStack() }) {
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
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Personagem não encontrado")
            }
        } else {
            Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${c.avatarEmoji} ${c.name}", style = MaterialTheme.typography.headlineSmall)
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Região: ${c.region}")
                        Text("Idade: ${c.age}")
                        Text("Classe: ${c.clazz}")
                        Text("Nível: ${c.level}")
                    }
                }
                Text("Atributos", style = MaterialTheme.typography.titleMedium)
                AttributeStat("Inteligência", c.attributes.intelligence)
                AttributeStat("Destreza", c.attributes.dexterity)
                AttributeStat("Força", c.attributes.strength)
                AttributeStat("Agilidade", c.attributes.agility)
                AttributeStat("Carisma", c.attributes.charisma)
                Text("Pontos restantes: ${c.availablePoints}")
            }
        }
    }
}

@Composable
private fun AttributeStat(label: String, value: Int) {
    fun attrBar(v: Int) = (v.coerceIn(0, 50)) / 50f
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            LinearProgressIndicator(progress = { attrBar(value) }, modifier = Modifier.fillMaxWidth())
            Text("$value / 50", style = MaterialTheme.typography.bodySmall)
        }
    }
}
