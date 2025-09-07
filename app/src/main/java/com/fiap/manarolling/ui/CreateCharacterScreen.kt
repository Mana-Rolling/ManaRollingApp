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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fiap.manarolling.R
import com.fiap.manarolling.model.Attributes
import com.fiap.manarolling.model.Character
import com.fiap.manarolling.model.ClassPresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCharacterScreen(
    vm: CharacterViewModel,
    nav: NavController,
    // callback chamado após salvar; por padrão vai para a História
    onCreated: (Character) -> Unit = { created ->
        nav.navigate("${Routes.STORY}/${created.id}") {
            popUpTo(Routes.LIST) { inclusive = false }
        }
    }
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("1") }

    // Foto (upload) + default
    var photoUri by remember { mutableStateOf<String?>(null) }
    val defaultResUri = remember {
        "android.resource://${context.packageName}/drawable/default_character"
    }
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            photoUri = it.toString()
            // mantém permissão de leitura
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Throwable) { }
        }
    }

    // Classe com presets
    val classOptions = remember { ClassPresets.options }          // lista de nomes
    var clazz by remember { mutableStateOf(classOptions.first()) }
    var clazzExpanded by remember { mutableStateOf(false) }

    // Atributos (com piso da classe e teto 50) + pontos extras
    var intel by remember { mutableStateOf(5) }
    var dex by remember { mutableStateOf(5) }
    var str by remember { mutableStateOf(5) }
    var agi by remember { mutableStateOf(5) }
    var cha by remember { mutableStateOf(5) }
    var points by remember { mutableStateOf(10) }

    // base da classe selecionada
    val base: Attributes by remember(clazz) {
        mutableStateOf(ClassPresets.base[clazz] ?: Attributes())
    }

    // Ao trocar de classe: volta atributos para o base e reseta pontos extras
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

    // Helpers
    fun inc(set: (Int) -> Unit, v: Int) {
        if (points > 0 && v < 50) { set(v + 1); points-- }
    }
    fun dec(set: (Int) -> Unit, v: Int, floor: Int) {
        if (v > floor) { set(v - 1); points++ }
    }
    fun bar(v: Int) = (v.coerceIn(0, 50)) / 50f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Personagem") },
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
                .verticalScroll(rememberScrollState())
        ) {

            // Foto
            ElevatedCard(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                            Spacer(Modifier.height(6.dp))
                            Text("Selecionar foto")
                        }
                    }
                }
            }

            // Campos básicos
            Column(
                Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    placeholder = { Text("Ex.: Aria") },
                    leadingIcon = { Icon(Icons.Filled.Flag, null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

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
                    onValueChange = { age = it.filter(Char::isDigit) },
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

                // Classe (dropdown)
                ExposedDropdownMenuBox(
                    expanded = clazzExpanded,
                    onExpandedChange = { clazzExpanded = !clazzExpanded }
                ) {
                    OutlinedTextField(
                        value = clazz,
                        onValueChange = {},
                        readOnly = true,
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

                OutlinedTextField(
                    value = level,
                    onValueChange = { level = it.filter(Char::isDigit) },
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

                Text(
                    "Pontos disponíveis: $points",
                    style = MaterialTheme.typography.titleMedium
                )

                // Linha de atributo reutilizável
                @Composable
                fun RowAttr(title: String, value: Int, floor: Int, set: (Int) -> Unit) {
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
                                    progress = { bar(value) },
                                    modifier = Modifier.fillMaxWidth(),
                                    trackColor = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    "$value / 50 (mín: $floor)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { dec(set, value, floor) },
                                    enabled = value > floor
                                ) { Text("-") }
                                Button(
                                    onClick = { inc(set, value) },
                                    enabled = points > 0 && value < 50
                                ) { Text("+") }
                            }
                        }
                    }
                }

                // Atributos
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
                            photoUri = photoUri ?: defaultResUri, // grava default se não escolheu
                            attributes = Attributes(
                                intelligence = intel,
                                dexterity = dex,
                                strength = str,
                                agility = agi,
                                charisma = cha
                            )
                        )
                        vm.addCharacter(c)
                        onCreated(c) // <- devolve para quem chamou decidir o fluxo
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Salvar")
                }
            }
        }
    }
}
