package com.fiap.manarolling.ui.character

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.ui.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListCharacterScreen(
    vm: CharacterViewModel,
    nav: NavController
) {
    val characters by vm.characters.collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Seus personagens") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate(Routes.CREATE) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Novo personagem") }
            )
        }
    ) { pad ->
        if (characters.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(pad)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Você ainda não tem personagens. Toque em “Novo personagem”.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(characters, key = { it.id }) { c ->
                CharacterRow(
                    character = c,
                    onClick = { nav.navigate("${Routes.DETAIL}/${c.id}") }
                )
            }
            item { Spacer(Modifier.height(84.dp)) } // respiro pro FAB
        }
    }
}

@Composable
private fun CharacterRow(
    character: Character,
    onClick: () -> Unit
) {
    val vidaAttr = runCatching { character.attributes.vida }.getOrDefault(0)
    val hpMax = runCatching { character.vitals.hpMax }.getOrDefault(vidaAttr)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(character.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val clazz = character.clazz.ifBlank { "—" }
                Text("Classe: $clazz")
                Spacer(Modifier.width(8.dp))
                Text("•")
                Spacer(Modifier.width(8.dp))
                Text("Nível: ${character.level}")
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(onClick = {}, enabled = false, label = { Text("HP Máx: $hpMax") })
            }
        }
    }
}
