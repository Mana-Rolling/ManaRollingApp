package com.fiap.manarolling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fiap.manarolling.model.Attributes
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.ui.character.CharacterViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineCharactersScreen(
    onBack: () -> Unit,
    characterVm: CharacterViewModel = viewModel()
) {
    val chars by characterVm.characters.collectAsState()

    var showCreate by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var clazz by remember { mutableStateOf("Aventureiro") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus personagens (offline)") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Voltar") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) { Text("+") }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text("Total: ${chars.size}")
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(chars) { ch ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(ch.name, style = MaterialTheme.typography.titleMedium)
                            Text("Classe: ${ch.clazz} | Nível: ${ch.level}")
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Novo personagem (offline)") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = clazz, onValueChange = { clazz = it }, label = { Text("Classe") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val uid = Firebase.auth.currentUser?.uid.orEmpty()
                    val character = Character(
                        id = System.currentTimeMillis(),
                        name = name.ifBlank { "Sem nome" },
                        clazz = clazz.ifBlank { "Aventureiro" },
                        level = 1,
                        attributes = Attributes(),
                        ownerUid = uid   // <- ATRELADO AO DONO mesmo offline
                    )
                    characterVm.addCharacter(character)
                    name = ""; clazz = ""
                    showCreate = false
                }) { Text("Criar") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancelar") } }
        )
    }
}
