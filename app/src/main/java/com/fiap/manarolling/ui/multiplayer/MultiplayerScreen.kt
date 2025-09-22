package com.fiap.manarolling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.multiplayer.GameSession
import com.fiap.manarolling.multiplayer.MultiplayerViewModel
import com.fiap.manarolling.ui.character.CharacterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerScreen(
    sessionId: String,
    onLeave: () -> Unit,
    onOpenCharacter: (ownerUid: String, charId: Long) -> Unit,
    onCreateCharacter: () -> Unit,
    vm: MultiplayerViewModel,
    characterVm: CharacterViewModel
) {
    // Garante listeners desta sessão
    LaunchedEffect(sessionId) {
        vm.startListening(sessionId)
        vm.startCharactersListener(sessionId)
    }
    DisposableEffect(sessionId) {
        onDispose {
            vm.stopListening(sessionId)
            vm.stopCharactersListener(sessionId)
        }
    }

    val session: GameSession? by vm.sessionState.collectAsState(null)
    val charsByOwner: Map<String, List<Character>> by vm.sessionCharacters.collectAsState(emptyMap())
    val isMaster = vm.isMaster()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = session?.let { s -> "Sessão ${s.id}" } ?: "Sessão"
                    Text(title)
                },
                navigationIcon = {
                    IconButton(onClick = onLeave) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val id = sessionId
                        vm.leaveSession(id) { _, _ -> onLeave() }
                    }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Sair")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isMaster) {
                ExtendedFloatingActionButton(
                    onClick = onCreateCharacter,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Criar personagem") }
                )
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabeçalho da sessão
            SessionHeader(session = session, isMaster = isMaster)

            // Lista de personagens por jogador
            if (charsByOwner.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum personagem conectado ainda.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    charsByOwner.forEach { (ownerUid, list) ->
                        item(key = "owner-$ownerUid-header") {
                            OwnerHeader(
                                ownerUid = ownerUid,
                                isMaster = session?.masterId == ownerUid,
                                isMe = vm.myUid.collectAsState().value == ownerUid
                            )
                        }
                        items(list, key = { it.id }) { ch ->
                            CharacterRowItem(
                                sessionId = sessionId,
                                ownerUid = ownerUid,
                                character = ch,
                                onOpen = { onOpenCharacter(ownerUid, ch.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionHeader(session: GameSession?, isMaster: Boolean) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Detalhes da sessão", style = MaterialTheme.typography.titleMedium)
            val id = session?.id ?: "—"
            val master = session?.masterName ?: "—"
            Text("Código: $id")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mestre: $master")
                if (isMaster) {
                    Spacer(Modifier.width(8.dp))
                    AssistChip(onClick = {}, enabled = false, label = { Text("Você é o mestre") })
                }
            }
        }
    }
}

@Composable
private fun OwnerHeader(ownerUid: String, isMaster: Boolean, isMe: Boolean) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val tags = buildList {
            if (isMaster) add("Mestre")
            if (isMe) add("Você")
        }.joinToString(" • ")
        Text(
            text = if (tags.isBlank()) "Jogador" else "Jogador – $tags",
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
private fun CharacterRowItem(
    sessionId: String,
    ownerUid: String,
    character: Character,
    onOpen: () -> Unit
) {
    val vidaAttr = runCatching { character.attributes.vida }.getOrDefault(0)
    val hpMax = runCatching { character.vitals.hpMax }.getOrDefault(vidaAttr)
    val hpNow = runCatching { character.runtime.hp }.getOrDefault(hpMax)
    val manaMax = runCatching { character.vitals.manaMax }.getOrDefault(20)
    val manaNow = runCatching { character.runtime.mana }.getOrDefault(20)

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(character.name, style = MaterialTheme.typography.titleMedium)
            Text("Classe: ${character.clazz.ifBlank { "—" }}  •  Nível: ${character.level}")

            // Chips de status
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(onClick = {}, enabled = false, label = { Text("HP: $hpNow/$hpMax") })
                AssistChip(onClick = {}, enabled = false, label = { Text("Mana: $manaNow/$manaMax") })
            }

            // Ação abrir ficha
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onOpen) { Text("Abrir") }
            }
        }
    }
}
