package com.fiap.manarolling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fiap.manarolling.model.Attributes
import com.fiap.manarolling.model.ClassPresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCharacterScreen(vm: CharacterViewModel, id: Long, nav: NavController) {
    val current = vm.getCharacter(id)
    if (current == null) {
        Scaffold(topBar = { TopAppBar(title = { Text("Editar") }) }) { pad ->
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Personagem não encontrado")
            }
        }
        return
    }

    var name by remember(current.id) { mutableStateOf(current.name) }
    var region by remember(current.id) { mutableStateOf(current.region) }
    var age by remember(current.id) { mutableStateOf(current.age.toString()) }
    var level by remember(current.id) { mutableStateOf(current.level.toString()) }

    val emojiOptions = listOf("🧙","🗡️","🛡️","🏹","🧝","🐺")
    var avatarEmoji by remember(current.id) { mutableStateOf(current.avatarEmoji.ifBlank { emojiOptions.first() }) }
    var emojiExpanded by remember { mutableStateOf(false) }

    val classOptions = ClassPresets.options
    var clazz by remember(current.id) { mutableStateOf(if (current.clazz in classOptions) current.clazz else classOptions.first()) }
    var clazzExpanded by remember { mutableStateOf(false) }

    var intel by remember(current.id) { mutableStateOf(current.attributes.intelligence) }
    var dex by remember(current.id) { mutableStateOf(current.attributes.dexterity) }
    var str by remember(current.id) { mutableStateOf(current.attributes.strength) }
    var agi by remember(current.id) { mutableStateOf(current.attributes.agility) }
    var cha by remember(current.id) { mutableStateOf(current.attributes.charisma) }

    var points by remember(current.id) { mutableStateOf(current.availablePoints) }

    // Baseline atual da classe (piso)
    val base: Attributes by remember(clazz) {
        mutableStateOf(ClassPresets.base[clazz] ?: Attributes())
    }

    // Ao mudar a classe no editor:
    // - aplica os valores base
    // - reseta pool de pontos para 10
    LaunchedEffect(clazz) {
        ClassPresets.base[clazz]?.let { b ->
            intel = b.intelligence
            dex   = b.dexterity
            str   = b.strength
            agi   = b.agility
            cha   = b.charisma
        }
        points = 10
    }

    fun inc(set: (Int)->Unit, v: Int) { if (points > 0 && v < 50) { set(v+1); points-- } }
    fun dec(set: (Int)->Unit, v: Int, floor: Int) { if (v > floor) { set(v-1); points++ } }
    fun attrBar(v: Int) = (v.coerceIn(0, 50)) / 50f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Personagem") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Avatar + Nome
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(expanded = emojiExpanded, onExpandedChange = { emojiExpanded = !emojiExpanded }) {
                    OutlinedTextField(
                        value = avatarEmoji, onValueChange = {}, readOnly = true,
                        label = { Text("Avatar") }, leadingIcon = { Icon(Icons.Filled.Badge, null) },
                        modifier = Modifier.menuAnchor().width(110.dp)
                    )
                    ExposedDropdownMenu(expanded = emojiExpanded, onDismissRequest = { emojiExpanded = false }) {
                        emojiOptions.forEach { e -> DropdownMenuItem(text = { Text(e) }, onClick = { avatarEmoji = e; emojiExpanded = false }) }
                    }
                }
                Spacer(Modifier.width(12.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nome") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = region, onValueChange = { region = it },
                label = { Text("Região") }, singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = age, onValueChange = { age = it.filter { ch -> ch.isDigit() } },
                label = { Text("Idade") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            // Classe (dropdown)
            ExposedDropdownMenuBox(expanded = clazzExpanded, onExpandedChange = { clazzExpanded = !clazzExpanded }) {
                OutlinedTextField(
                    value = clazz, onValueChange = {}, readOnly = true,
                    label = { Text("Classe") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clazzExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = clazzExpanded, onDismissRequest = { clazzExpanded = false }) {
                    classOptions.forEach { opt -> DropdownMenuItem(text = { Text(opt) }, onClick = { clazz = opt; clazzExpanded = false }) }
                }
            }

            OutlinedTextField(
                value = level, onValueChange = { level = it.filter { ch -> ch.isDigit() } },
                label = { Text("Nível") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Pontos disponíveis: $points", style = MaterialTheme.typography.titleMedium)

            @Composable
            fun RowAttr(title: String, value: Int, floor: Int, set: (Int)->Unit) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.titleSmall)
                            LinearProgressIndicator(progress = { attrBar(value) }, modifier = Modifier.fillMaxWidth())
                            Text("$value / 50 (mín: $floor)", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            OutlinedButton(
                                onClick = { dec(set, value, floor) },
                                enabled = value > floor
                            ) { Text("-") }
                            Spacer(Modifier.height(6.dp))
                            Button(
                                onClick = { inc(set, value) },
                                enabled = points > 0 && value < 50
                            ) { Text("+") }
                        }
                    }
                }
            }

            RowAttr("Inteligência", intel, base.intelligence) { intel = it }
            RowAttr("Destreza",     dex,   base.dexterity)    { dex = it }
            RowAttr("Força",        str,   base.strength)     { str = it }
            RowAttr("Agilidade",    agi,   base.agility)      { agi = it }
            RowAttr("Carisma",      cha,   base.charisma)     { cha = it }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val updated = current.copy(
                        name = name.trim(),
                        region = region.trim(),
                        age = age.toIntOrNull() ?: 0,
                        clazz = clazz.trim(),
                        level = level.toIntOrNull() ?: current.level,
                        availablePoints = points,
                        avatarEmoji = avatarEmoji,
                        attributes = Attributes(intel, dex, str, agi, cha)
                    )
                    vm.updateCharacter(updated)
                    nav.popBackStack()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Salvar alterações")
            }
        }
    }
}
