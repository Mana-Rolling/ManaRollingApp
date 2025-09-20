package com.fiap.manarolling.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fiap.manarolling.multiplayer.MultiplayerViewModel
import com.fiap.manarolling.model.Character as MRCharacter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    navToSession: (String) -> Unit,
    vm: MultiplayerViewModel,
    characterVm: CharacterViewModel
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // picker de personagem
    var showPicker by remember { mutableStateOf(false) }
    var selected: MRCharacter? by remember { mutableStateOf(null) }
    val localChars by remember { derivedStateOf { characterVm.characters.value } }

    Scaffold(topBar = { TopAppBar(title = { Text("Conectar à sessão") }) }) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Seu nome") }, singleLine = true)
            OutlinedTextField(value = code, onValueChange = { code = it.uppercase().take(6) }, label = { Text("Código (6 chars)") }, singleLine = true)

            Button(
                onClick = {
                    if (localChars.isEmpty()) error = "Crie um personagem primeiro na aba Personagens."
                    else { selected = localChars.firstOrNull(); showPicker = true }
                },
                enabled = name.isNotBlank() && code.length == 6,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Entrar na sessão") }

            Button(
                onClick = {
                    if (name.isBlank()) { error = "Informe seu nome"; return@Button }
                    vm.createSession(name) { sessionId, err ->
                        if (sessionId != null) {
                            vm.startListening(sessionId); vm.startCharactersListener(sessionId)
                            navToSession(sessionId)
                        } else error = "Falha ao criar: ${err?.message}"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Criar sessão (mestre)") }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Escolha seu personagem") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(localChars) { ch ->
                        ListItem(
                            headlineContent = { Text(ch.name) },
                            supportingContent = { Text("Classe: ${ch.clazz} • Nível: ${ch.level}") },
                            trailingContent = {
                                RadioButton(selected = (selected?.id == ch.id), onClick = { selected = ch })
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Divider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val ch = selected ?: return@TextButton
                    vm.joinWithCharacter(code, name, ch) { ok, err ->
                        if (ok) {
                            vm.startListening(code); vm.startCharactersListener(code)
                            showPicker = false
                            navToSession(code)
                        } else {
                            showPicker = false
                            error = "Falha ao entrar: ${err?.message}"
                        }
                    }
                }) { Text("Entrar") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancelar") } }
        )
    }
}
