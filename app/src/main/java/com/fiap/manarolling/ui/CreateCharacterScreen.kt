package com.fiap.manarolling.ui

import coil.compose.AsyncImage
import com.fiap.manarolling.R
import androidx.compose.ui.layout.ContentScale
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fiap.manarolling.model.Attributes
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.model.ClassPresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCharacterScreen(vm: CharacterViewModel, nav: NavController) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("1") }

    // ✅ foto do personagem
    var photoUri by remember { mutableStateOf<String?>(null) }
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            photoUri = it.toString()
            // em Android ≤12, manter permissão persistente
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Throwable) {}
        }
    }

    // Classe (dropdown) com presets e piso
    val classOptions = ClassPresets.options
    var clazz by remember { mutableStateOf(classOptions.first()) }
    var clazzExpanded by remember { mutableStateOf(false) }

    var intel by remember { mutableStateOf(5) }
    var dex by remember { mutableStateOf(5) }
    var str by remember { mutableStateOf(5) }
    var agi by remember { mutableStateOf(5) }
    var cha by remember { mutableStateOf(5) }
    var points by remember { mutableStateOf(10) }

    val base by remember(clazz) { mutableStateOf(ClassPresets.base[clazz] ?: Attributes()) }

    LaunchedEffect(clazz) {
        ClassPresets.base[clazz]?.let { b ->
            intel = b.intelligence; dex = b.dexterity; str = b.strength; agi = b.agility; cha = b.charisma
        }
        points = 10 // resetar pool
    }

    fun inc(set: (Int)->Unit, v: Int) { if (points > 0 && v < 50) { set(v+1); points-- } }
    fun dec(set: (Int)->Unit, v: Int, floor: Int) { if (v > floor) { set(v-1); points++ } }
    fun bar(v: Int) = (v.coerceIn(0, 50)) / 50f

    Scaffold(topBar = { TopAppBar(title = { Text("Criar Personagem") }) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState())) {

            // ==== Cabeçalho com Foto (estilo mock): capa larga com cantos arredondados ====
            ElevatedCard(
                Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable {
                            pickMedia.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = photoUri ?: R.drawable.default_character,
                        contentDescription = "Foto do personagem",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // ==== Form ====
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nome") }, placeholder = { Text("Ex.: Aria") },
                    leadingIcon = { Icon(Icons.Filled.Flag, null) },
                    singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
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

                // Classe
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
                fun RowAttr(title: String, value: Int, floor: Int, set: (Int)->Unit) {
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(title, style = MaterialTheme.typography.titleSmall)
                                LinearProgressIndicator(progress = { bar(value) }, modifier = Modifier.fillMaxWidth())
                                Text("$value / 50 (mín: $floor)", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { dec(set, value, floor) }, enabled = value > floor) { Text("-") }
                                Button(onClick = { inc(set, value) }, enabled = points > 0 && value < 50) { Text("+") }
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
                            photoUri = photoUri,
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
}
