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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.fiap.manarolling.R
import com.fiap.manarolling.model.Attributes
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.model.ClassPresets
import com.fiap.manarolling.model.RuntimeVitals
import com.fiap.manarolling.model.Vitals
import com.fiap.manarolling.ui.character.CharacterViewModel

private const val VIDA_CAP = 50

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCharacterScreen(
    onBack: () -> Unit,
    onCreated: (Character) -> Unit,
    vm: SettingsViewModel,
    repoVM: CharacterViewModel
) {
    val context = LocalContext.current

    // --------- Estado do formulário ---------
    var name by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    // Classe vinda dos presets existentes
    val classOptions = remember { ClassPresets.options }
    var clazz by remember { mutableStateOf(classOptions.firstOrNull() ?: "") }
    var clazzExpanded by remember { mutableStateOf(false) }

    fun baseFor(c: String): Attributes = ClassPresets.base[c] ?: Attributes()

    // Atributos iniciam pelos presets da classe
    var intel by remember { mutableStateOf(baseFor(clazz).intelligence) }
    var dex   by remember { mutableStateOf(baseFor(clazz).dexterity) }
    var str   by remember { mutableStateOf(baseFor(clazz).strength) }
    var agi   by remember { mutableStateOf(baseFor(clazz).agility) }
    var cha   by remember { mutableStateOf(baseFor(clazz).charisma) }

    // Flags "touched": se o usuário mexeu, não sobrescrevemos ao trocar de classe
    var touchedInt by remember { mutableStateOf(false) }
    var touchedDex by remember { mutableStateOf(false) }
    var touchedStr by remember { mutableStateOf(false) }
    var touchedAgi by remember { mutableStateOf(false) }
    var touchedCha by remember { mutableStateOf(false) }

    // Vida baseada no preset da classe, com CAP e também com "touched"
    var vida by remember { mutableStateOf(baseFor(clazz).vida.coerceAtMost(VIDA_CAP)) }
    var vidaTouched by remember { mutableStateOf(false) }

    // Nível e pontos (mantidos como no app)
    var level by remember { mutableStateOf("1") }
    var points by remember { mutableStateOf(10) }

    // Ao trocar de classe, atualiza os atributos NÃO editados manualmente
    LaunchedEffect(clazz) {
        val b = baseFor(clazz)
        if (!touchedInt) intel = b.intelligence
        if (!touchedDex) dex   = b.dexterity
        if (!touchedStr) str   = b.strength
        if (!touchedAgi) agi   = b.agility
        if (!touchedCha) cha   = b.charisma
        if (!vidaTouched) vida = b.vida.coerceAtMost(VIDA_CAP)
    }

    // Picker de imagem
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
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
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

        // Classe (usando ClassPresets.options)
        ExposedDropdownMenuBox(
            expanded = clazzExpanded,
            onExpandedChange = { clazzExpanded = !clazzExpanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = clazz,
                onValueChange = {},
                label = { Text("Classe") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clazzExpanded) },
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

        Spacer(Modifier.height(12.dp))

        Text("Pontos disponíveis: $points", style = MaterialTheme.typography.titleMedium)

        // ===== Componente genérico de atributo: usa pontos (±1) =====
        @Composable
        fun RowAttr(
            title: String,
            value: Int,
            floor: Int,
            onChanged: (Int) -> Unit,
            onTouched: () -> Unit
        ) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // -1 (devolve 1 ponto se acima do piso)
                        FilledTonalButton(
                            onClick = {
                                if (value > floor) {
                                    onChanged(value - 1)
                                    points++
                                    onTouched()
                                }
                            },
                            enabled = value > floor
                        ) { Text("-") }

                        Text("$value", style = MaterialTheme.typography.titleMedium)

                        // +1 (consome 1 ponto se houver disponível)
                        FilledTonalButton(
                            onClick = {
                                if (points > 0) {
                                    onChanged(value + 1)
                                    points--
                                    onTouched()
                                }
                            },
                            enabled = points > 0
                        ) { Text("+") }
                    }
                }
            }
        }

        // ===== Vida (HP Máx) usando os MESMOS pontos (sem ±5) =====
        RowAttr(
            title = "Vida (HP Máx)",
            value = vida,
            floor = baseFor(clazz).vida, // piso = preset da classe
            onChanged = { v ->
                // aplica CAP na Vida
                vida = v.coerceAtMost(VIDA_CAP)
            },
            onTouched = { vidaTouched = true }
        )

        Spacer(Modifier.height(8.dp))
        RowAttr("Inteligência", intel, 1, { intel = it }, { touchedInt = true })
        Spacer(Modifier.height(8.dp))
        RowAttr("Destreza",     dex,   1, { dex   = it }, { touchedDex = true })
        Spacer(Modifier.height(8.dp))
        RowAttr("Força",        str,   1, { str   = it }, { touchedStr = true })
        Spacer(Modifier.height(8.dp))
        RowAttr("Agilidade",    agi,   1, { agi   = it }, { touchedAgi = true })
        Spacer(Modifier.height(8.dp))
        RowAttr("Carisma",      cha,   1, { cha   = it }, { touchedCha = true })

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val vidaFinal = vida.coerceAtMost(VIDA_CAP)
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
                        charisma = cha,
                        vida = vidaFinal
                    ),
                    vitals = Vitals(
                        hpMax = vidaFinal,
                        manaMax = 20
                    ),
                    runtime = RuntimeVitals(
                        hp = vidaFinal,
                        mana = 20
                    )
                )

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
