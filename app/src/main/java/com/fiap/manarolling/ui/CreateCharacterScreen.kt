package com.fiap.manarolling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fiap.manarolling.model.Attributes
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.model.ClassPresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCharacterScreen(vm: CharacterViewModel, nav: NavController) {
    var name by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("1") }

    val emojiOptions = listOf("🧙", "🗡️", "🛡️", "🏹", "🧝", "🐺")
    var avatarEmoji by remember { mutableStateOf(emojiOptions.first()) }
    var emojiExpanded by remember { mutableStateOf(false) }


    val classOptions = ClassPresets.options
    var clazz by remember { mutableStateOf(classOptions.first()) }
    var clazzExpanded by remember { mutableStateOf(false) }


    var intel by remember { mutableStateOf(5) }
    var dex by remember { mutableStateOf(5) }
    var str by remember { mutableStateOf(5) }
    var agi by remember { mutableStateOf(5) }
    var cha by remember { mutableStateOf(5) }

    // Pontos extras para distribuir
    var points by remember { mutableStateOf(20) }

    // Baseline atual da classe (piso para decremento)
    val base: Attributes by remember(clazz) {
        mutableStateOf(ClassPresets.base[clazz] ?: Attributes())
    }

    // Sempre que mudar a classe:
    // 1) aplica os atributos base da classe
    // 2) reseta os pontos extras para 10
    LaunchedEffect(clazz) {
        ClassPresets.base[clazz]?.let { b ->
            intel = b.intelligence
            dex   = b.dexterity
            str   = b.strength
            agi   = b.agility
            cha   = b.charisma
        }
        points = 20
    }

    fun inc(setter: (Int) -> Unit, current: Int) {
        if (points > 0 && current < 50) { setter(current + 1); points-- }
    }

    // agora o decremento respeita o piso (baseline) da classe
    fun dec(setter: (Int) -> Unit, current: Int, floor: Int) {
        if (current > floor) { setter(current - 1); points++ }
    }

    fun attrBar(v: Int) = (v.coerceIn(0, 50)) / 50f

    Scaffold(topBar = { TopAppBar(title = { Text("Criar Personagem") }) }) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar + Nome
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(expanded = emojiExpanded, onExpandedChange = { emojiExpanded = !emojiExpanded }) {
                    OutlinedTextField(
                        value = avatarEmoji, onValueChange = {}, readOnly = true,
                        label = { Text("Avatar") }, leadingIcon = { Icon(Icons.Filled.Badge, null) },
                        modifier = Modifier.menuAnchor().width(110.dp)
                    )
                    ExposedDropdownMenu(expanded = emojiExpanded, onDismissRequest = { emojiExpanded = false }) {
                        emojiOptions.forEach { e ->
                            DropdownMenuItem(text = { Text(e) }, onClick = { avatarEmoji = e; emojiExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nome") }, placeholder = { Text("Ex.: Aria") },
                    leadingIcon = { Icon(Icons.Filled.Flag, null) },
                    singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = region, onValueChange = { region = it },
                label = { Text("Região") }, placeholder = { Text("Ex.: Eldoria") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
                singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = age, onValueChange = { age = it.filter { ch -> ch.isDigit() } },
                label = { Text("Idade") }, placeholder = { Text("Ex.: 22") },
                leadingIcon = { Icon(Icons.Filled.Cake, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                singleLine = true, modifier = Modifier.fillMaxWidth()
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
                    classOptions.forEach { opt ->
                        DropdownMenuItem(text = { Text(opt) }, onClick = { clazz = opt; clazzExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = level, onValueChange = { level = it.filter { ch -> ch.isDigit() } },
                label = { Text("Nível") }, placeholder = { Text("Ex.: 1") },
                leadingIcon = { Icon(Icons.Filled.SportsMartialArts, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )

            Text("Pontos disponíveis: $points", style = MaterialTheme.typography.titleMedium)

            @Composable
            fun RowAttr(title: String, value: Int, floor: Int, set: (Int) -> Unit) {
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
                    val c = Character(
                        name = name.trim(),
                        region = region.trim(),
                        age = age.toIntOrNull() ?: 0,
                        clazz = clazz.trim(),
                        level = level.toIntOrNull() ?: 1,
                        availablePoints = points,
                        avatarEmoji = avatarEmoji,
                        attributes = Attributes(intel, dex, str, agi, cha)
                    )
                    vm.addCharacter(c)
                    nav.navigate("${Routes.STORY}/${c.id}") {
                        popUpTo(Routes.LIST) { inclusive = false }
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Salvar e Ir para História")
            }
        }
    }
}
