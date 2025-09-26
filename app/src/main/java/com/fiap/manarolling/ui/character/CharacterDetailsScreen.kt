package com.fiap.manarolling.ui.character

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fiap.manarolling.R
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.model.ClassPresets
import com.fiap.manarolling.ui.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    vm: CharacterViewModel,
    id: Long,
    nav: NavController
) {
    val c: Character? = vm.getCharacter(id)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personagem") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (c != null) {
                        IconButton(onClick = { nav.navigate("${Routes.EDIT}/$id") }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = {
                            vm.deleteCharacter(c.id)
                            nav.popBackStack()
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                        }
                    }
                }
            )
        }
    ) { pad ->
        if (c == null) {
            Box(
                Modifier.padding(pad).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Personagem não encontrado") }
            return@Scaffold
        }

        // Fallbacks para compatibilidade com fichas antigas
        val hpMax  = try { c.vitals.hpMax }  catch (_: Throwable) { 0 }
        val manaMx = try { c.vitals.manaMax } catch (_: Throwable) { 20 }
        val hpNow  = try { c.runtime.hp }     catch (_: Throwable) { hpMax }
        val manaNow= try { c.runtime.mana }   catch (_: Throwable) { 20 }

        val context = LocalContext.current

        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Capa
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        val model = c.photoUri ?: ""
                        val dfltChar = painterResource(R.drawable.default_character)
                        if (model.isNotBlank()){
                            AsyncImage(
                                model = model,
                                placeholder = painterResource(R.drawable.default_character),
                                error = painterResource(R.drawable.default_character),
                                contentDescription = "Imagem do personagem",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = dfltChar,
                                contentDescription = "Imagem do personagem",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Info básicas
            item {
                ElevatedCard(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(c.name, style = MaterialTheme.typography.titleLarge)
                        if (c.clazz.isNotBlank()) Text("Classe: ${c.clazz}")
                        Text("Nível: ${c.level}")
                        if (c.region.isNotBlank()) Text("Região: ${c.region}")
                        if (c.age > 0) Text("Idade: ${c.age}")
                    }
                }
            }

            // === NOVO: Arma & Habilidade da classe ===
            item {
                val loadout = ClassPresets.loadoutFor(c.clazz)
                if (loadout != null) {
                    ElevatedCard(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Arma & Habilidade (${c.clazz})", style = MaterialTheme.typography.titleMedium)

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Arma
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Arma", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val weaponResId = context.resources.getIdentifier(
                                            loadout.weaponImageRes,
                                            "drawable",
                                            context.packageName
                                        )
                                        if (weaponResId != 0) {
                                            Image(
                                                painter = painterResource(id = weaponResId),
                                                contentDescription = "Arma",
                                                modifier = Modifier.size(40.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Column {
                                            Text(loadout.weaponName, style = MaterialTheme.typography.bodyLarge)
                                            Text("Dano: ${loadout.weaponDamage}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }

                                // Habilidade
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Habilidade", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val abilityResId = context.resources.getIdentifier(
                                            loadout.abilityImageRes,
                                            "drawable",
                                            context.packageName
                                        )
                                        if (abilityResId != 0) {
                                            Image(
                                                painter = painterResource(id = abilityResId),
                                                contentDescription = "Habilidade",
                                                modifier = Modifier.size(40.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        Column {
                                            Text(loadout.abilityName, style = MaterialTheme.typography.bodyLarge)
                                            val effect = if (loadout.healing) "Cura: ${loadout.abilityPower}" else "Dano: ${loadout.abilityPower}"
                                            Text(effect, style = MaterialTheme.typography.bodySmall)
                                            AssistChip(onClick = {}, enabled = false, label = { Text("Mana: ${loadout.manaCost}") })
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    OutlinedCard(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            "Defina uma classe para ver a arma e a habilidade.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            // === FIM DO NOVO BLOCO ===

            // Barras de HP / Mana (somente exibição no local/offline)
            if (hpMax > 0) {
                item {
                    StatBar(
                        label = "HP",
                        current = hpNow.coerceIn(0, hpMax),
                        max = hpMax
                    )
                }
            }
            item {
                StatBar(
                    label = "Mana",
                    current = manaNow.coerceIn(0, manaMx),
                    max = manaMx
                )
            }

            // Atributos
            item {
                Text(
                    "Atributos",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item { AttributeStat("Inteligência", c.attributes.intelligence) }
            item { AttributeStat("Destreza",     c.attributes.dexterity) }
            item { AttributeStat("Força",        c.attributes.strength) }
            item { AttributeStat("Agilidade",    c.attributes.agility) }
            item { AttributeStat("Carisma",      c.attributes.charisma) }
            // NOVO: exibir o atributo de Vida (que agora existe no model)
            item { AttributeStat("VIDA (atributo)", c.attributes.vida) }

            // História / Capítulos
            item {
                ElevatedCard(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("História e capítulos") },
                        supportingContent = {
                            val qtd = c.story.chapters.size
                            Text(if (qtd == 0) "Nenhum capítulo" else "$qtd capítulos")
                        },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.Notes, null) },
                        trailingContent = {
                            TextButton(onClick = { nav.navigate("${Routes.STORY}/${c.id}") }) {
                                Text("Abrir")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttributeStat(label: String, value: Int) {
    fun bar(v: Int) = (v.coerceIn(0, 50)) / 50f
    ElevatedCard(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            LinearProgressIndicator(
                progress = { bar(value) },
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.outline
            )
            Text("$value / 50", style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Barra simples para HP/Mana (apenas visual nesta tela). */
@Composable
private fun StatBar(
    label: String,
    current: Int,
    max: Int
) {
    val progress = if (max <= 0) 0f else (current.toFloat() / max.toFloat()).coerceIn(0f, 1f)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label)
            Text("$current / $max")
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            trackColor = MaterialTheme.colorScheme.outline
        )
    }
}
