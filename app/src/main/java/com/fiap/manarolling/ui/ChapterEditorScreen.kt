package com.fiap.manarolling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fiap.manarolling.model.Chapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterEditorScreen(
    vm: CharacterViewModel,
    characterId: Long,
    nav: NavController,
    chapterId: Long? = null
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val today = remember { sdf.format(Date()) }

    // Estados
    var title by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(today) }
    var content by remember { mutableStateOf("") }

    // Se for edição, pré-carrega os dados
    LaunchedEffect(chapterId) {
        if (chapterId != null) {
            val ch = vm.getCharacter(characterId)?.story?.chapters?.firstOrNull { it.id == chapterId }
            if (ch != null) {
                title = ch.title
                dateStr = ch.date.ifBlank { today }
                content = ch.content
            }
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    fun formatMillis(millis: Long) = sdf.format(Date(millis))

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateStr = formatMillis(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (chapterId == null) "Novo capítulo" else "Editar capítulo") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Título") }, singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = dateStr, onValueChange = { dateStr = it },
                    label = { Text("Data (AAAA-MM-DD)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
                FilledIconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = "Selecionar data")
                }
            }

            OutlinedTextField(
                value = content, onValueChange = { content = it },
                label = { Text("Conteúdo") }, minLines = 6,
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp)
            )

            Button(
                onClick = {
                    if (chapterId == null) {
                        vm.addChapter(characterId, Chapter(title = title.trim(), date = dateStr.trim(), content = content.trim()))
                    } else {
                        vm.updateChapter(characterId, Chapter(id = chapterId, title = title.trim(), date = dateStr.trim(), content = content.trim()))
                    }
                    nav.popBackStack() // volta para a lista
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("Salvar capítulo")
            }
        }
    }
}
