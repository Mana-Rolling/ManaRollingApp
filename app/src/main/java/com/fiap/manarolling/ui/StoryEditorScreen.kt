package com.fiap.manarolling.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fiap.manarolling.model.Chapter
import com.fiap.manarolling.model.Character
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryEditorScreen(
    vm: CharacterViewModel,
    characterId: Long,
    nav: NavController
) {

    val chars = vm.characters.collectAsState().value
    val c: Character? = chars.firstOrNull { it.id == characterId }


    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    var chapterIdEditing by remember { mutableStateOf<Long?>(null) }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today) }
    var content by remember { mutableStateOf("") }

    fun clearForm() {
        chapterIdEditing = null
        title = ""
        date = today
        content = ""
    }

    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("História ${if (c != null) "de ${c.name}" else ""}") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {

            FloatingActionButton(onClick = { clearForm() }) {
                Icon(Icons.Filled.Add, contentDescription = "Novo capítulo")
            }
        }
    ) { pad ->
        if (c == null) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Personagem não encontrado")
            }
            return@Scaffold
        }

        Column(
            Modifier.padding(pad).fillMaxSize()
        ) {

            Text(
                "Capítulos",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 8.dp)
            )

            val chapters = c.story.chapters
            if (chapters.isEmpty()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Ainda não há capítulos")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    items(chapters, key = { it.id }) { ch ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            onClick = {
                                // Carrega no formulário para edição
                                chapterIdEditing = ch.id
                                title = ch.title
                                date = ch.date
                                content = ch.content
                            }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(ch.title.ifBlank { "(Sem título)" },
                                    style = MaterialTheme.typography.titleMedium)
                                Text("Data: ${ch.date.ifBlank { "—" }}",
                                    style = MaterialTheme.typography.bodySmall)
                                if (ch.content.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = ch.content.take(160) + if (ch.content.length > 160) "…" else "",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = {
                                        vm.deleteChapter(c.id, ch.id)
                                        scope.launch { snackbar.showSnackbar("Capítulo excluído") }
                                        if (chapterIdEditing == ch.id) clearForm()
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Excluir")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Divider()


            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    if (chapterIdEditing == null) "Novo capítulo" else "Editar capítulo",
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data (AAAA-MM-DD)") },
                    placeholder = { Text(today) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Conteúdo") },
                    minLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (chapterIdEditing == null) {
                                vm.addChapter(
                                    c.id,
                                    Chapter(
                                        title = title.trim(),
                                        date = date.trim(),
                                        content = content.trim()
                                    )
                                )
                                scope.launch { snackbar.showSnackbar("Capítulo adicionado") }
                            } else {
                                vm.updateChapter(
                                    c.id,
                                    Chapter(
                                        id = chapterIdEditing!!,
                                        title = title.trim(),
                                        date = date.trim(),
                                        content = content.trim()
                                    )
                                )
                                scope.launch { snackbar.showSnackbar("Capítulo atualizado") }
                            }
                            clearForm()
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (chapterIdEditing == null) "Salvar capítulo" else "Salvar alterações")
                    }

                    if (chapterIdEditing != null) {
                        TextButton(onClick = { clearForm() }) { Text("Cancelar") }
                    }
                }
            }
        }
    }
}
