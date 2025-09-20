package com.fiap.manarolling.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fiap.manarolling.multiplayer.MultiplayerViewModel
import com.fiap.manarolling.model.Character as MRCharacter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionCharacterDetailsScreen(
    sessionId: String,
    ownerUid: String,
    charId: Long,
    nav: NavController,
    vm: MultiplayerViewModel = viewModel(LocalContext.current as ComponentActivity)
) {
    // garantia (deep link)
    LaunchedEffect(sessionId) {
        vm.startListening(sessionId)
        vm.startCharactersListener(sessionId)
    }

    val charsByOwner by vm.sessionCharacters.collectAsState()
    val ch: MRCharacter? = charsByOwner[ownerUid]?.firstOrNull { it.id == charId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ch?.name ?: "Personagem") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { pad ->
        if (ch == null) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Carregando ficha…")
            }
        } else {
            Column(
                Modifier.padding(pad).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Classe: ${ch.clazz}", style = MaterialTheme.typography.titleMedium)
                Text("Nível: ${ch.level}")
                if (!ch.region.isNullOrBlank()) Text("Região: ${ch.region}")
                if (ch.age > 0) Text("Idade: ${ch.age}")

                Divider()
                Text("Atributos", style = MaterialTheme.typography.titleMedium)
                Text("INT: ${ch.attributes.intelligence}")
                Text("DEX: ${ch.attributes.dexterity}")
                Text("FOR: ${ch.attributes.strength}")
                Text("AGI: ${ch.attributes.agility}")
                Text("CAR: ${ch.attributes.charisma}")

                // adicione mais campos da sua ficha conforme necessário
            }
        }
    }
}
