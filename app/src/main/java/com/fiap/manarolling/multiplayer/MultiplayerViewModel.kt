package com.fiap.manarolling.multiplayer

import com.fiap.manarolling.model.Character as MRCharacter
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MultiplayerViewModel : ViewModel() {

    private val repo = MultiplayerRepository()

    /* ===== UID (auth anônima) ===== */
    private val _myUid = MutableStateFlow(Firebase.auth.currentUser?.uid)
    val myUid: StateFlow<String?> = _myUid

    private fun ensureUid(then: (String) -> Unit, onError: ((Exception) -> Unit)? = null) {
        val curr = _myUid.value
        if (curr != null) return then(curr)
        Firebase.auth.signInAnonymously()
            .addOnSuccessListener { res -> res.user?.uid?.let { _myUid.value = it; then(it) } }
            .addOnFailureListener { onError?.invoke(it) }
    }

    /* ===== Sessão ===== */
    private val _sessionState = MutableStateFlow<GameSession?>(null)
    val sessionState: StateFlow<GameSession?> = _sessionState

    fun createSession(masterName: String, callback: (String?, Exception?) -> Unit) {
        ensureUid({ uid -> repo.createSession(uid, masterName, callback) }, { callback(null, it) })
    }

    fun joinWithCharacter(sessionId: String, playerName: String, character: MRCharacter, callback: (Boolean, Exception?) -> Unit) {
        ensureUid({ uid -> repo.joinSessionWithCharacter(sessionId, uid, playerName, character, callback) },
            { callback(false, it) })
    }

    fun startListening(sessionId: String) {
        repo.startListening(sessionId) { _sessionState.value = it }
    }
    fun stopListening(sessionId: String) = repo.removeSessionListener(sessionId)

    /* ===== Personagens da sessão ===== */
    private val _sessionCharacters = MutableStateFlow<Map<String, List<MRCharacter>>>(emptyMap())
    val sessionCharacters: StateFlow<Map<String, List<MRCharacter>>> = _sessionCharacters

    fun startCharactersListener(sessionId: String) {
        repo.listenCharacters(sessionId) { _sessionCharacters.value = it }
    }
    fun stopCharactersListener(sessionId: String) = repo.removeCharactersListener(sessionId)

    /* util */
    fun isMaster(): Boolean = _sessionState.value?.masterId == _myUid.value
}
