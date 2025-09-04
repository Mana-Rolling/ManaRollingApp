package com.fiap.manarolling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// ✅ imports corretos
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fiap.manarolling.model.Attributes
import com.fiap.manarolling.model.Character

@OptIn(ExperimentalMaterial3Api::class) // ExposedDropdownMenuBox/TopAppBar
@Composable
fun CreateCharacterScreen(vm: CharacterViewModel, nav: NavController) {
    var name by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var clazz by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("1") }


    val emojiOptions = listOf("🧙", "🗡️", "🛡️", "🏹", "🧝", "🐺")
    var avatarEmoji by remember { mutableStateOf(emojiOptions.first()) }
    var emojiExpanded by remember { mutableStateOf(false) }

    var intel by remember { mutableStateOf(5) }
    var dex by remember { mutableStateOf(5) }
    var str by remember { mutableStateOf(5) }
    var points by remember { mutableStateOf(10) }

    fun inc(setter: (Int) -> Unit, current: Int) {
        if (points > 0 && current < 20) { setter(current + 1); points-- }
    }
    fun dec(setter: (Int) -> Unit, current: Int) {
        if (current > 0) { setter(current - 1); points++ }
    }
    fun attrBar(value: Int) = value / 20f

    Scaffold(topBar = { TopAppBar(title = { Text("Criar Personagem") }) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar + Nome
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(
                    expanded = emojiExpanded,
                    onExpandedChange = { emojiExpanded = !emojiExpanded }
                ) {
                    OutlinedTextField(
                        value = avatarEmoji,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Avatar") },
                        leadingIcon = { Icon(Icons.Filled.Badge, null) },
                        modifier = Modifier.menuAnchor().width(110.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = emojiExpanded,
                        onDismissRequest = { emojiExpanded = false }
                    ) {
                        emojiOptions.forEach { e ->
                            DropdownMenuItem(
                                text = { Text(e) },
                                onClick = { avatarEmoji = e; emojiExpanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    placeholder = { Text("Ex.: Aria") },
                    leadingIcon = { Icon(Icons.Filled.Flag, null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = region,
                onValueChange = { region = it },
                label = { Text("Região") },
                placeholder = { Text("Ex.: Eldoria") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { ch -> ch.isDigit() } },
                label = { Text("Idade") },
                placeholder = { Text("Ex.: 22") },
                leadingIcon = { Icon(Icons.Filled.Cake, null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = clazz,
                onValueChange = { clazz = it },
                label = { Text("Classe") },
                placeholder = { Text("Ex.: Mago, Arqueiro...") },
                leadingIcon = { Icon(Icons.Filled.Category, null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = level,
                onValueChange = { level = it.filter { ch -> ch.isDigit() } },
                label = { Text("Nível") },
                placeholder = { Text("Ex.: 1") },
                leadingIcon = { Icon(Icons.Filled.SportsMartialArts, null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Pontos disponíveis: $points", style = MaterialTheme.typography.titleMedium)


            @Composable
            fun RowAttr(title: String, value: Int, set: (Int) -> Unit) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(title, style = MaterialTheme.typography.titleSmall)
                            LinearProgressIndicator(
                                progress = { attrBar(value) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("$value / 20", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            OutlinedButton(onClick = { dec(set, value) }) { Text("-") }
                            Spacer(Modifier.height(6.dp))
                            Button(onClick = { inc(set, value) }) { Text("+") }
                        }
                    }
                }
            }

            RowAttr("Inteligência", intel) { intel = it }
            RowAttr("Destreza", dex) { dex = it }
            RowAttr("Força", str) { str = it }

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
                        attributes = Attributes(intel, dex, str)
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
                Text("Salvar e Escrever História")
            }
        }
    }
}
