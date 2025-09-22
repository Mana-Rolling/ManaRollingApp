package com.fiap.manarolling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.ui.character.CharacterViewModel
import androidx.compose.foundation.text.KeyboardOptions
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCharacterScreen(
    vm: CharacterViewModel,
    id: Long,
    nav: NavController
) {
    val c: Character? = vm.getCharacter(id)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar personagem") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            if (c != null) {
                BottomAppBar {
                    Spacer(Modifier.weight(1f))
                    FilledTonalButton(
                        onClick = {
                            // ação de salvar é disparada no botão do conteúdo (para ter acesso aos states)
                        },
                        enabled = false // visual apenas (botão real fica na tela)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Salvar")
                    }
                }
            }
        }
    ) { pad ->
        if (c == null) {
            Box(
                Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Personagem não encontrado")
            }
            return@Scaffold
        }

        // ====== STATES ======
        val currentHpMax = try { c.vitals.hpMax } catch (_: Throwable) { 0 }
        val currentManaMax = try { c.vitals.manaMax } catch (_: Throwable) { 20 }

        var vidaText by remember {
            mutableStateOf(
                if (currentHpMax > 0) currentHpMax.toString()
                else maxOf(0, c.runtime.hp).toString()
            )
        }

        val hpNow = try { c.runtime.hp } catch (_: Throwable) { currentHpMax }
        val manaNow = try { c.runtime.mana } catch (_: Throwable) { 20 }

        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabeçalho simples
            Text(c.name, style = MaterialTheme.typography.titleLarge)
            if (c.clazz.isNotBlank()) Text("Classe: ${c.clazz}")
            Text("Nível: ${c.level}")

            HorizontalDivider()

            // ===== Campo Vida (HP Máx) =====
            OutlinedTextField(
                value = vidaText,
                onValueChange = { txt -> vidaText = txt.filter { it.isDigit() } },
                leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                label = { Text("Vida (HP Máx)") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Exibição dos valores atuais (somente leitura)
            AssistChipBar(label = "HP atual", value = hpNow)
            AssistChipBar(label = "Mana atual", value = manaNow)
            AssistChipBar(label = "Mana Máx (fixo)", value = 20)

            Spacer(Modifier.height(8.dp))

            // ===== Botão SALVAR =====
            Button(
                onClick = {
                    val hpMax = vidaText.toIntOrNull() ?: 0
                    val updated = c.copy(
                        vitals = c.vitals.copy(
                            hpMax = hpMax,
                            manaMax = 20 // regra fixa
                        ),
                        runtime = c.runtime.copy(
                            hp = min(c.runtime.hp, hpMax.coerceAtLeast(0)),
                            mana = min(c.runtime.mana, 20)
                        )
                    )
                    vm.updateCharacter(updated)
                    nav.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Salvar alterações")
            }
        }
    }
}

@Composable
private fun AssistChipBar(label: String, value: Int) {
    AssistChip(
        onClick = {},
        label = { Text("$label: $value") },
        enabled = false
    )
}
