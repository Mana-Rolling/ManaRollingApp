package com.fiap.manarolling.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fiap.manarolling.data.CharacterRepository
import com.fiap.manarolling.data.FileCharacterStorage
import com.fiap.manarolling.model.Chapter
import com.fiap.manarolling.model.Character
import kotlinx.coroutines.flow.StateFlow

class CharacterViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CharacterRepository(
        store = FileCharacterStorage(app),
        scope = viewModelScope
    )

    val characters: StateFlow<List<Character>> = repo.characters

    fun addCharacter(c: Character) = repo.add(c)
    fun updateCharacter(c: Character) = repo.update(c)
    fun deleteCharacter(id: Long) = repo.delete(id)
    fun getCharacter(id: Long): Character? = repo.get(id)

    fun addChapter(characterId: Long, chapter: Chapter) = repo.addChapter(characterId, chapter)
    fun updateChapter(characterId: Long, chapter: Chapter) = repo.updateChapter(characterId, chapter)
    fun deleteChapter(characterId: Long, chapterId: Long) = repo.deleteChapter(characterId, chapterId)

}
