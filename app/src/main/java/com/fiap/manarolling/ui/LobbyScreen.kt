package com.fiap.manarolling.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fiap.manarolling.multiplayer.MultiplayerViewModel
import com.fiap.manarolling.multiplayer.PlayerInfo

@Composable
fun LobbyScreen(
    onCreated: (sessionId: String) -> Unit,
    onJoined: (sessionId: String) -> Unit,
    vm: MultiplayerViewModel = viewModel(),
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Seu nome")
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth())

        Button(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            onClick = {
            if (name.isBlank()) {
                message = "Digite um nome"
                return@Button
            }
            vm.createSession(masterId = name, masterName = name) { sessionId, error ->
                if (sessionId != null) {

                    Log.d("CADE_ESSE_ID", sessionId)
                    onCreated(sessionId)
                } else {
                    message = "Erro ao criar sala: ${'$'}{error?.message}"
                }
            }
        }) {
            Text(text = "Criar sala (sou mestre)")
        }

        Text(modifier = Modifier.padding(top = 16.dp), text = "Entrar em uma sala")
        OutlinedTextField(value = code, onValueChange = { code = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Código da sala") })

        Button(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), onClick = {
            if (name.isBlank() || code.isBlank()) {
                message = "Digite nome e código da sala"
                return@Button
            }
            val player = PlayerInfo(id = name, name = name)
            vm.joinSession(code, player) { success, error ->
                if (success) onJoined(code) else message = "Falha ao entrar: ${'$'}{error?.message}"
            }
        }) {
            Text(text = "Entrar como jogador")
        }

        if (message.isNotBlank()) {
//            Text(modifier = Modifier.padding(top = 12.dp), text = message)
            Log.d("CADE_ESSE_ID", message)
        }
    }
}
