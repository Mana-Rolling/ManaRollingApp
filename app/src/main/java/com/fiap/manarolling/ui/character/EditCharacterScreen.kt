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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fiap.manarolling.R
import com.fiap.manarolling.model.Attributes
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.model.ClassPresets
import com.fiap.manarolling.model.RuntimeVitals
import com.fiap.manarolling.model.Vitals
import com.fiap.manarolling.ui.character.CharacterViewModel
import kotlin.math.max

private const val VIDA_CAP = 50

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCharacterScreen(
    id: Long,
    nav: NavController,
    repoVM: CharacterViewModel
) {
    val original: Character? = remember(id) { repoVM.getCharacter(id) }
    val context = LocalContext.current

    if (original == null) {
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
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Personagem não encontrado")
            }
        }
        return
    }

    // --------- Estado do formulário (pré-preenchido) ---------
    var name by remember { mutableStateOf(original.name) }
    var region by remember { mutableStateOf(original.region) }
    var age by remember { mutableStateOf(if (original.age > 0) original.age.toString() else "") }
    var photoUri by remember { mutableStateOf(original.photoUri) }

    // Classe / opções
    val classOptions = remember { ClassPresets.options }
    var clazz by remember { mutableStateOf(original.clazz.ifBlank { classOptions.firstOrNull() ?: "" }) }
    var clazzExpanded by remember { mutableStateOf(false) }

    fun baseFor(c: String): Attributes = ClassPresets.base[c] ?: Attributes()

    // Atributos iniciam do personagem (respeitando CAP e piso)
    var intel by remember { mutableStateOf(max(original.attributes.intelligence, baseFor(clazz).intelligence)) }
    var dex   by remember { mutableStateOf(max(original.attributes.dexterity,    baseFor(clazz).dexterity)) }
    var str   by remember { mutableStateOf(max(original.attributes.strength,     baseFor(clazz).strength)) }
    var agi   by remember { mutableStateOf(max(original.attributes.agility,      baseFor(clazz).agility)) }
    var cha   by remember { mutableStateOf(max(original.attributes.charisma,     baseFor(clazz).charisma)) }
    var vida  by remember { mutableStateOf(max(original.attributes.vida,         baseFor(clazz).vida).coerceAtMost(VIDA_CAP)) }

    var level by remember { mutableStateOf(original.level.toString()) }
    var points by remember { mutableStateOf(original.availablePoints) }

    // Ao trocar de classe, elevar atributos que ficaram abaixo do NOVO piso (sem cobrar pontos)
    LaunchedEffect(clazz) {
        val b = baseFor(clazz)
        fun bump(cur: Int, floor: Int) = if (cur < floor) floor else cur
        intel = bump(intel, b.intelligence)
        dex   = bump(dex,  b.dexterity)
        str   = bump(str,  b.strength)
        agi   = bump(agi,  b.agility)
        cha   = bump(cha,  b.charisma)
        vida  = bump(vida, b.vida).coerceAtMost(VIDA_CAP)
    }

    // Picker de imagem (mesma UX da criação)
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
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Foto
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            ) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = photoUri ?: painterResource(R.drawable.default_character),
                        placeholder = painterResource(R.drawable.default_character),
                        error = painterResource(R.drawable.default_character),
                        contentDescription = "Foto/ilustração do personagem",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .padding(4.dp)
                    )
                    Text("Toque para escolher imagem", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Filled.Image, contentDescription = null)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = region,
                onValueChange = { region = it },
                label = { Text("Região") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

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

            // ===== Classe (dropdown) =====
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
                            onClick = {
                                clazz = opt
                                clazzExpanded = false
                            }
                        )
                    }
                }
            }

            // ===== Arma & Habilidade (preview) =====
            val loadout = ClassPresets.loadoutFor(clazz)
            if (loadout != null) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Arma & Habilidade ($clazz)", style = MaterialTheme.typography.titleMedium)

                        val weaponResId = context.resources.getIdentifier(
                            loadout.weaponImageRes, "drawable", context.packageName
                        )
                        val abilityResId = context.resources.getIdentifier(
                            loadout.abilityImageRes, "drawable", context.packageName
                        )

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Arma", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (weaponResId != 0) {
                                        Icon(painter = painterResource(weaponResId), contentDescription = "Arma", modifier = Modifier.size(40.dp))
                                    }
                                    Column {
                                        Text(loadout.weaponName, style = MaterialTheme.typography.bodyLarge)
                                        Text("Dano: ${loadout.weaponDamage}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Habilidade", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (abilityResId != 0) {
                                        Icon(painter = painterResource(abilityResId), contentDescription = "Habilidade", modifier = Modifier.size(40.dp))
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
            }

            // ===== Nível =====
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

            // ===== Pontos disponíveis =====
            Text("Pontos disponíveis: $points", style = MaterialTheme.typography.titleMedium)

            // Componente inline para atributo com piso por classe e pontos
            @Composable
            fun RowAttr(
                title: String,
                value: Int,
                floor: Int,
                onChanged: (Int) -> Unit
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
                            FilledTonalButton(
                                onClick = {
                                    if (value > floor) {
                                        onChanged(value - 1)
                                        points++
                                    }
                                },
                                enabled = value > floor
                            ) { Text("–") }

                            Text("$value", style = MaterialTheme.typography.titleMedium)

                            FilledTonalButton(
                                onClick = {
                                    if (points > 0) {
                                        onChanged(value + 1)
                                        points--
                                    }
                                },
                                enabled = points > 0
                            ) { Text("+") }
                        }
                    }
                }
            }

            val piso = baseFor(clazz)
            RowAttr("Vida (HP Máx)", vida, piso.vida) { v -> vida = v.coerceAtMost(VIDA_CAP) }
            RowAttr("Inteligência",  intel, piso.intelligence) { intel = it }
            RowAttr("Destreza",      dex,   piso.dexterity)    { dex   = it }
            RowAttr("Força",         str,   piso.strength)     { str   = it }
            RowAttr("Agilidade",     agi,   piso.agility)      { agi   = it }
            RowAttr("Carisma",       cha,   piso.charisma)     { cha   = it }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val vidaFinal = vida.coerceAtMost(VIDA_CAP)
                    val updated = original.copy(
                        name = name.trim(),
                        region = region.trim(),
                        age = age.toIntOrNull() ?: 0,
                        clazz = clazz.trim(),
                        level = level.toIntOrNull() ?: original.level,
                        availablePoints = points,
                        photoUri = photoUri ?: "android.resource://${context.packageName}/${R.drawable.default_character}",
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
                            hp = original.runtime.hp.coerceIn(0, vidaFinal),
                            mana = original.runtime.mana.coerceIn(0, 20)
                        )
                    )
                    repoVM.updateCharacter(updated)
                    nav.popBackStack()
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Salvar alterações")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
