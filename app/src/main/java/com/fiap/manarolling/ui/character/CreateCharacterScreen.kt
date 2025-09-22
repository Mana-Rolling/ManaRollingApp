package com.fiap.manarolling.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fiap.manarolling.R
import com.fiap.manarolling.model.Attributes
import com.fiap.manarolling.model.Vitals
import com.fiap.manarolling.model.RuntimeVitals
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.model.ClassPresets
import com.fiap.manarolling.ui.character.CharacterViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCharacterScreen(
    onBack: () -> Unit,
    onCreated: (Character) -> Unit,
    vm: SettingsViewModel,
    repoVM: CharacterViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    // Classe + presets
    val classOptions = remember { ClassPresets.options }
    var clazz by remember { mutableStateOf(classOptions.firstOrNull() ?: "") }
    var clazzExpanded by remember { mutableStateOf(false) }

    // Nível
    var level by remember { mutableStateOf("1") }
    // NOVO: Vida (HP Máx)
    var vida by remember { mutableStateOf("10") }

    // Pontos disponíveis para atributos
    var points by remember { mutableStateOf(10) }

    // Atributos
    var intel by remember { mutableStateOf(5) }
    var dex by remember { mutableStateOf(5) }
    var str by remember { mutableStateOf(5) }
    var agi by remember { mutableStateOf(5) }
    var cha by remember { mutableStateOf(5) }

    // Imagem padrão se não escolher foto
    val defaultResUri = "android.resource://${context.packageName}/${R.drawable.default_character}"

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            photoUri = it.toString()
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Throwable) { }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Text("Criar personagem", style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(Modifier.height(12.dp))

        // Foto
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Foto do personagem",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Image, contentDescription = null)
                        Text("Toque para escolher uma imagem")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = region,
            onValueChange = { region = it },
            label = { Text("Região") },
            leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it.filter(Char::isDigit) },
            label = { Text("Idade") },
            leadingIcon = { Icon(Icons.Filled.Cake, null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Classe
        ExposedDropdownMenuBox(
            expanded = clazzExpanded,
            onExpandedChange = { clazzExpanded = !clazzExpanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = clazz,
                onValueChange = {},
                label = { Text("Classe") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = clazzExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = clazzExpanded,
                onDismissRequest = { clazzExpanded = false }
            ) {
                classOptions.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = { clazz = opt; clazzExpanded = false }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = level,
            onValueChange = { level = it.filter(Char::isDigit) },
            label = { Text("Nível") },
            leadingIcon = { Icon(Icons.Filled.SportsMartialArts, null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // NOVO: Vida (HP Máx)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = vida,
            onValueChange = { vida = it.filter(Char::isDigit) },
            label = { Text("Vida (HP Máx)") },
            leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Text("Pontos disponíveis: $points", style = MaterialTheme.typography.titleMedium)

        @Composable
        fun RowAttr(title: String, value: Int, floor: Int, set: (Int) -> Unit) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = { if (value > floor) { set(value - 1); points++ } },
                            enabled = value > floor
                        ) { Text("-") }
                        Text("$value", style = MaterialTheme.typography.titleMedium)
                        FilledTonalButton(
                            onClick = { if (points > 0) { set(value + 1); points-- } },
                            enabled = points > 0
                        ) { Text("+") }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        RowAttr("Inteligência", intel, 1) { intel = it }
        Spacer(Modifier.height(8.dp))
        RowAttr("Destreza", dex, 1) { dex = it }
        Spacer(Modifier.height(8.dp))
        RowAttr("Força", str, 1) { str = it }
        Spacer(Modifier.height(8.dp))
        RowAttr("Agilidade", agi, 1) { agi = it }
        Spacer(Modifier.height(8.dp))
        RowAttr("Carisma", cha, 1) { cha = it }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val c = Character(
                    name = name.trim(),
                    region = region.trim(),
                    age = age.toIntOrNull() ?: 0,
                    clazz = clazz.trim(),
                    level = level.toIntOrNull() ?: 1,
                    availablePoints = points,
                    photoUri = photoUri ?: defaultResUri,
                    attributes = Attributes(
                        intelligence = intel,
                        dexterity = dex,
                        strength = str,
                        agility = agi,
                        charisma = cha
                    ),
                    // NOVO: vitais e estado atual
                    vitals = Vitals(
                        hpMax = (vida.toIntOrNull() ?: 0),
                        manaMax = 20
                    ),
                    runtime = RuntimeVitals(
                        hp = (vida.toIntOrNull() ?: 0).coerceAtLeast(0),
                        mana = 20
                    )
                )

                // Somente local (jogador NÃO cria diretamente na sessão)
                repoVM.addCharacter(c)
                onCreated(c)
            },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Salvar personagem")
        }

        Spacer(Modifier.height(24.dp))
    }
}
