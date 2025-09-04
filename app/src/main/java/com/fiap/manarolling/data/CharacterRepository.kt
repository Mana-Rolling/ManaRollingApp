package com.fiap.manarolling.data

import com.fiap.manarolling.model.Character
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CharacterRepository(
    private val store: FileCharacterStorage,
    private val scope: CoroutineScope
) {
    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters

    init {
        // carrega do JSON ao iniciar
        scope.launch(Dispatchers.IO) {
            _characters.value = store.readAll()
        }
    }

    fun add(character: Character) {
        val updated = _characters.value + character
        _characters.value = updated
        persist(updated)
    }

    fun update(character: Character) {
        val updated = _characters.value.map { if (it.id == character.id) character else it }
        _characters.value = updated
        persist(updated)
    }

    fun delete(id: Long) {
        val updated = _characters.value.filterNot { it.id == id }
        _characters.value = updated
        persist(updated)
    }

    fun get(id: Long): Character? = _characters.value.firstOrNull { it.id == id }

    private fun persist(list: List<Character>) {
        // grava em arquivo fora da UI thread
        scope.launch(Dispatchers.IO) { store.writeAll(list) }
    }
}