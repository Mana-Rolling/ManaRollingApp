package com.fiap.manarolling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fiap.manarolling.multiplayer.MultiplayerViewModel
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionCharacterDetailsScreen(
    sessionId: String,
    ownerUid: String,
    charId: Long,
    nav: NavController,
    vm: MultiplayerViewModel
) {
    // Listeners da sessão
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

    val charsByOwner by vm.sessionCharacters.collectAsState()
    val isMaster = vm.isMaster()

    val ch = remember(charsByOwner, ownerUid, charId) {
        charsByOwner[ownerUid]?.firstOrNull { it.id == charId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ch?.name ?: "Personagem") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { pad ->
        if (ch == null) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Carregando ficha…")
            }
            return@Scaffold
        }

        val hpMax = ch.vitals.hpMax
        val manaMax = ch.vitals.manaMax

        var hp by remember(ch.id, ch.runtime.hp) {
            mutableStateOf(ch.runtime.hp.coerceIn(0, max(0, hpMax)))
        }
        var mana by remember(ch.id, ch.runtime.mana) {
            mutableStateOf(ch.runtime.mana.coerceIn(0, max(0, manaMax)))
        }

        fun applyHp(newVal: Int) {
            if (!isMaster || hpMax <= 0) return
            val coerced = newVal.coerceIn(0, hpMax)
            hp = coerced
            vm.setHp(sessionId, ownerUid, charId, coerced, hpMax)
        }

        fun applyMana(newVal: Int) {
            if (!isMaster || manaMax <= 0) return
            val coerced = newVal.coerceIn(0, manaMax)
            mana = coerced
            vm.setMana(sessionId, ownerUid, charId, coerced, manaMax)
        }

        Column(
            Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabeçalho
            Text("Classe: ${ch.clazz}", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, enabled = false, label = { Text("Nível: ${ch.level}") })
                val vidaAttr = runCatching { ch.attributes.vida }.getOrDefault(hpMax)
                AssistChip(onClick = {}, enabled = false, label = { Text("VIDA (atributo): $vidaAttr") })
            }

            HorizontalDivider()

            // ====== HP (apenas ±1 e Set) ======
            StatBarInline(
                label = "HP",
                current = hp,
                max = hpMax,
                editable = isMaster && hpMax > 0,
                onMinus = { applyHp(hp - 1) },
                onPlus  = { applyHp(hp + 1) },
                onSet   = { v -> applyHp(v) }
            )

            // ====== Mana (apenas ±1 e Set) ======
            StatBarInline(
                label = "Mana",
                current = mana,
                max = manaMax,
                editable = isMaster && manaMax > 0,
                onMinus = { applyMana(mana - 1) },
                onPlus  = { applyMana(mana + 1) },
                onSet   = { v -> applyMana(v) }
            )

            HorizontalDivider()

            Text("Atributos", style = MaterialTheme.typography.titleMedium)
            Text("INT: ${ch.attributes.intelligence}")
            Text("DEX: ${ch.attributes.dexterity}")
            Text("FOR: ${ch.attributes.strength}")
            Text("AGI: ${ch.attributes.agility}")
            Text("CAR: ${ch.attributes.charisma}")
        }
    }
}

/** Barra de status inline com ±1 e campo Set (sem ±5). */
@Composable
private fun StatBarInline(
    label: String,
    current: Int,
    max: Int,
    editable: Boolean,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onSet: (Int) -> Unit
) {
    val progress = if (max <= 0) 0f else (current.toFloat() / max.toFloat()).coerceIn(0f, 1f)

    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label)
            Text("$current / $max")
        }
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())

        if (editable) {
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onMinus) { Text("-1") }
                OutlinedButton(onClick = onPlus)  { Text("+1") }

                var input by remember(current, max) { mutableStateOf(current.toString()) }
                OutlinedTextField(
                    value = input,
                    onValueChange = { txt ->
                        val clean = txt.filter { it.isDigit() }
                        input = clean
                        clean.toIntOrNull()?.let { onSet(it.coerceIn(0, max)) }
                    },
                    label = { Text("Set") },
                    singleLine = true,
                    modifier = Modifier.width(100.dp)
                )
            }
        }
    }
}
