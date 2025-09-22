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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.ui.character.CharacterViewModel
import kotlin.math.min

private const val VIDA_CAP = 50

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
        }
    ) { pad ->
        if (c == null) {
            Box(
                Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Personagem não encontrado") }
            return@Scaffold
        }

        // Valores atuais com fallback para fichas antigas
        val hpMaxAtual   = runCatching { c.vitals.hpMax }.getOrDefault(0)
        val vidaAttr     = runCatching { c.attributes.vida }.getOrDefault(hpMaxAtual)
        val hpAtual      = runCatching { c.runtime.hp }.getOrDefault(vidaAttr)
        val manaAtual    = runCatching { c.runtime.mana }.getOrDefault(20)

        var vida by remember(c.id) {
            mutableStateOf(
                when {
                    hpMaxAtual > 0 -> hpMaxAtual
                    vidaAttr   > 0 -> vidaAttr
                    else           -> 10
                }.coerceIn(8, VIDA_CAP)
            )
        }

        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(c.name, style = MaterialTheme.typography.titleLarge)
            if (c.clazz.isNotBlank()) Text("Classe: ${c.clazz}")
            Text("Nível: ${c.level}")

            HorizontalDivider()

            // ===== Vida (HP Máx) como botões ± com CAP =====
            VidaRow(
                value = vida,
                onChange = { vida = it.coerceIn(8, VIDA_CAP) }
            )

            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("HP atual: $hpAtual • Mana atual: $manaAtual • Mana Máx: 20") }
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val vidaFinal = vida.coerceIn(8, VIDA_CAP)
                    val updated = c.copy(
                        // atributo
                        attributes = c.attributes.copy(vida = vidaFinal),
                        // vitais
                        vitals = c.vitals.copy(
                            hpMax = vidaFinal,
                            manaMax = 20
                        ),
                        // runtime coerente com os máximos
                        runtime = c.runtime.copy(
                            hp = min(hpAtual, vidaFinal),
                            mana = min(manaAtual, 20)
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
private fun VidaRow(
    value: Int,
    onChange: (Int) -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Favorite, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Vida (HP Máx)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                Text("$value / $VIDA_CAP")
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(onClick = { onChange(value - 1) }) { Text("-1") }
                Text("$value", style = MaterialTheme.typography.titleLarge)
                FilledTonalButton(onClick = { onChange(value + 1) }) { Text("+1") }
            }
        }
    }
}
