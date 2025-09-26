package com.fiap.manarolling.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.multiplayer.MultiplayerViewModel
import com.fiap.manarolling.ui.character.CharacterViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    vm: MultiplayerViewModel,
    characterVm: CharacterViewModel,
    navToSession: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    // Estado da tela
    var playerName by rememberSaveable { mutableStateOf("") }
    var sessionCode by rememberSaveable { mutableStateOf("") }

    // Personagens locais
    val chars by characterVm.characters.collectAsState(initial = emptyList())

    // Seleção persistida
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    val selectedChar = remember(chars, selectedId) { chars.firstOrNull { it.id == selectedId } }

    // Diálogo de seleção
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snack) },
        topBar = { TopAppBar(title = { Text("Lobby – Escolha seu personagem") }) }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nome do jogador
            OutlinedTextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Seu nome") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Código da sessão
            OutlinedTextField(
                value = sessionCode,
                onValueChange = { sessionCode = it.uppercase().trim() },
                label = { Text("Código da sessão") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Ascii
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Campo "Personagem" – caixa clicável que abre o diálogo
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedChar?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Personagem") },
                    placeholder = {
                        Text(
                            if (chars.isEmpty()) "Você ainda não criou personagens"
                            else "Toque para escolher"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                // Overlay 100% clicável para garantir o toque
                Spacer(
                    Modifier
                        .matchParentSize()
                        .alpha(0f)
                        .clickable(enabled = chars.isNotEmpty()) { showDialog = true }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Ações
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Entrar em sessão existente
                FilledTonalButton(
                    onClick = {
                        val ch = selectedChar
                        when {
                            playerName.isBlank() ->
                                scope.launch { snack.showSnackbar("Informe seu nome.", withDismissAction = true) }
                            sessionCode.isBlank() ->
                                scope.launch { snack.showSnackbar("Informe o código da sessão.", withDismissAction = true) }
                            ch == null ->
                                scope.launch { snack.showSnackbar("Escolha um personagem.", withDismissAction = true) }
                            else -> {
                                vm.joinSessionWithCharacter(sessionCode, playerName, ch) { ok, err ->
                                    if (ok) navToSession(sessionCode) else scope.launch {
                                        snack.showSnackbar(err?.message ?: "Falha ao entrar na sessão.", withDismissAction = true)
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Entrar na sessão")
                }

                // Criar sessão (mestre) e entrar
                Button(
                    onClick = {
                        val ch = selectedChar
                        when {
                            playerName.isBlank() ->
                                scope.launch { snack.showSnackbar("Informe seu nome.", withDismissAction = true) }
                            ch == null ->
                                scope.launch { snack.showSnackbar("Escolha um personagem.", withDismissAction = true) }
                            else -> {
                                vm.createSession(masterName = playerName) { sessionId, err ->
                                    if (sessionId == null) {
                                        scope.launch {
                                            snack.showSnackbar(err?.message ?: "Falha ao criar sessão.", withDismissAction = true)
                                        }
                                        return@createSession
                                    }
                                    vm.joinSessionWithCharacter(sessionId, playerName, ch) { ok, jerr ->
                                        if (ok) navToSession(sessionId) else scope.launch {
                                            snack.showSnackbar(jerr?.message ?: "Sessão criada, mas não foi possível entrar.", withDismissAction = true)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.GroupAdd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Criar sessão (Mestre)")
                }
            }
        }

        // ===================== DIÁLOGO DE SELEÇÃO =====================
        if (showDialog) {
            SelectCharacterDialog(
                characters = chars,
                currentId = selectedId,
                onDismiss = { showDialog = false },
                onConfirm = { chosenId ->
                    selectedId = chosenId
                    showDialog = false
                }
            )
        }
    }
}

/* ---------- Diálogo (AlertDialog) com lista e radio button ---------- */

@Composable
private fun SelectCharacterDialog(
    characters: List<Character>,
    currentId: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    // Seleção temporária dentro do diálogo
    var temp by remember(characters, currentId) { mutableStateOf(currentId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { temp?.let(onConfirm) },
                enabled = temp != null
            ) { Text("Selecionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Selecione um personagem") },
        text = {
            if (characters.isEmpty()) {
                Text("Você ainda não criou personagens.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(characters, key = { it.id }) { c ->
                        CharacterRadioRow(
                            character = c,
                            selected = temp == c.id,
                            onSelect = { temp = c.id }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun CharacterRadioRow(
    character: Character,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val vidaAttr = runCatching { character.attributes.vida }.getOrDefault(0)
    val hpMax = runCatching { character.vitals.hpMax }.getOrDefault(vidaAttr)

    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(character.name, style = MaterialTheme.typography.titleMedium)
            Text("Classe: ${character.clazz.ifBlank { "—" }}  •  Nível: ${character.level}")
            Text("Vida (atrib.): $vidaAttr  •  HP Máx: $hpMax")
        }
    }
}
