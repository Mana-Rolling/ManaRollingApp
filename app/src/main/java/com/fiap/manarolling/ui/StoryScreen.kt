package com.fiap.manarolling.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fiap.manarolling.model.Character

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryScreen(vm: CharacterViewModel, characterId: Long, nav: NavController) {
    val chars = vm.characters.collectAsState().value
    val c: Character? = chars.firstOrNull { it.id == characterId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("História ${if (c != null) "de ${c.name}" else ""}") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (c != null) {
                FloatingActionButton(onClick = { nav.navigate("${Routes.CHAPTER_CREATE}/${c.id}") }) {
                    Icon(Icons.Filled.Add, contentDescription = "Criar capítulo")
                }
            }
        }
    ) { pad ->
        if (c == null) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Personagem não encontrado")
            }
            return@Scaffold
        }
        Column(Modifier.padding(pad).fillMaxSize()) {
            if (c.story.chapters.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ainda não há capítulos")
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(12.dp)) {
                    items(c.story.chapters, key = { it.id }) { ch ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .clickable { nav.navigate("${Routes.CHAPTER_EDIT}/${c.id}/${ch.id}") }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(ch.title.ifBlank { "(Sem título)" }, style = MaterialTheme.typography.titleMedium)
                                Text("Data: ${ch.date.ifBlank { "—" }}", style = MaterialTheme.typography.bodySmall)
                                if (ch.content.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(ch.content.take(160) + if (ch.content.length > 160) "…" else "")
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = {
                                        vm.deleteChapter(c.id, ch.id)
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Excluir")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
