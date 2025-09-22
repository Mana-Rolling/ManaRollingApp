package com.fiap.manarolling.ui.multiplayer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fiap.manarolling.multiplayer.MultiplayerViewModel
import com.fiap.manarolling.ui.character.CharacterViewModel
import com.fiap.manarolling.model.Character as MRCharacter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerScreen(
    sessionId: String,
    onLeave: () -> Unit,
    onOpenCharacter: (ownerUid: String, charId: Long) -> Unit,
    onCreateCharacter: () -> Unit,
    // >>> RECEBE AS VMs DA ACTIVITY <<<
    vm: MultiplayerViewModel,
    characterVm: CharacterViewModel
) {
    // mantém listeners ativos entre telas
    LaunchedEffect(sessionId) {
        vm.startListening(sessionId)
        vm.startCharactersListener(sessionId)
    }

    val session by vm.sessionState.collectAsState()
    val charsByOwner by vm.sessionCharacters.collectAsState()
    val myUid by vm.myUid.collectAsState()
    val isMaster = session?.masterId == myUid

    // espelhamento local opcional para o mestre
    LaunchedEffect(sessionId, charsByOwner, isMaster) {
        if (isMaster == true) {
            val incoming = charsByOwner.values.flatten()
            val localById = characterVm.characters.value.associateBy { it.id }.toMutableMap()
            for (c in incoming) {
                val old = localById[c.id]
                if (old == null) characterVm.addCharacter(c)
                else if (old != c) characterVm.updateCharacter(c)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sessão: $sessionId ${if (isMaster) "(Mestre)" else "(Jogador)"}") },
                actions = {
                    TextButton(onClick = {
                        vm.stopCharactersListener(sessionId)
                        vm.stopListening(sessionId)
                        onLeave()
                    }) { Text("Sair") }
                }
            )
        },
        floatingActionButton = {
            if (isMaster) FloatingActionButton(onClick = onCreateCharacter) { Text("+") }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text("Jogadores: ${session?.players?.size ?: 0}")
            Spacer(Modifier.height(8.dp))

            Text("Personagens na sessão: ${charsByOwner.values.sumOf { it.size }}")
            Spacer(Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                charsByOwner.forEach { (ownerUid, list) ->
                    item { Text("Dono: $ownerUid", style = MaterialTheme.typography.labelSmall) }
                    items(list) { ch: MRCharacter ->
                        ElevatedCard(
                            onClick = { onOpenCharacter(ownerUid, ch.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(ch.name, style = MaterialTheme.typography.titleMedium)
                                Text("Classe: ${ch.clazz} | Nível: ${ch.level}")
                            }
                        }
                    }
                }
            }
        }
    }
}
