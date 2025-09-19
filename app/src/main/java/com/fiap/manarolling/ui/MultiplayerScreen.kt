package com.fiap.manarolling.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fiap.manarolling.multiplayer.GameSession
import com.fiap.manarolling.multiplayer.PlayerInfo
import com.fiap.manarolling.multiplayer.MultiplayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerScreen(
    sessionId: String,
    playerId: String,
    viewModel: MultiplayerViewModel = viewModel(),
    onExit: () -> Unit
) {
    val sessionState by viewModel.sessionState.collectAsState()

    // Inicia escuta da sessão quando a tela abrir
    LaunchedEffect(sessionId) {
        viewModel.startListening(sessionId)
    }

    // Para quando a tela for destruída
    DisposableEffect(Unit) {
        onDispose {
            viewModel.leaveSession(sessionId, playerId)
            viewModel.stopListening(sessionId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sessão $sessionId") },
                actions = {
                    TextButton(onClick = onExit) {
                        Text("Sair")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (sessionState == null) {
                CircularProgressIndicator()
            } else {
                SessionContent(
                    session = sessionState!!,
                    onUpdateState = { newState ->
                        viewModel.updateState(sessionId, newState)
                    }
                )
            }
        }
    }
}

@Composable
fun SessionContent(
    session: GameSession,
    onUpdateState: (Map<String, Any>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Mestre: ${session.masterName}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Text("Jogadores conectados:", style = MaterialTheme.typography.titleSmall)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(session.players.values.toList()) { player ->
                PlayerRow(player)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão para simular mudança de estado (apenas exemplo)
        Button(
            onClick = {
                val newState = session.state.toMutableMap()
                newState["lastAction"] = "Mestre mudou algo!"
                onUpdateState(newState)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Atualizar estado (exemplo)")
        }
    }
}

@Composable
fun PlayerRow(player: PlayerInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(player.name)
            if (player.characterId != null) {
                Text("Personagem: ${player.characterId}")
            }
        }
    }
}