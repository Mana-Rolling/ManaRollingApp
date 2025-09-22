package com.fiap.manarolling.multiplayer

import com.fiap.manarolling.model.Character as MRCharacter
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MultiplayerViewModel : ViewModel() {

    private val repo = MultiplayerRepository()

    /* ===== UID (auth anônima) ===== */
    private val _myUid = MutableStateFlow(Firebase.auth.currentUser?.uid)
    val myUid: StateFlow<String?> = _myUid

    private fun ensureUid(
        then: (String) -> Unit,
        onError: ((Exception) -> Unit)? = null
    ) {
        val curr = _myUid.value
        if (curr != null) {
            then(curr); return
        }
        Firebase.auth.signInAnonymously()
            .addOnSuccessListener { res ->
                val uid = res.user?.uid
                _myUid.value = uid
                if (uid != null) then(uid) else onError?.invoke(IllegalStateException("UID nulo"))
            }
            .addOnFailureListener { e -> onError?.invoke(e) }
    }

    /* ===== Sessão (estado) ===== */
    private val _sessionState = MutableStateFlow<GameSession?>(null)
    val sessionState: StateFlow<GameSession?> = _sessionState

    fun createSession(masterName: String, callback: (String?, Exception?) -> Unit) {
        ensureUid(
            then = { uid -> repo.createSession(uid, masterName, callback) },
            onError = { callback(null, it) }
        )
    }

    fun joinSessionWithCharacter(
        sessionId: String,
        playerName: String,
        character: MRCharacter,
        callback: (Boolean, Exception?) -> Unit
    ) {
        ensureUid(
            then = { uid ->
                repo.joinSessionWithCharacter(sessionId, uid, playerName, character, callback)
            },
            onError = { callback(false, it) }
        )
    }

    fun startListening(sessionId: String) {
        repo.startListening(sessionId) { _sessionState.value = it }
    }

    fun stopListening(sessionId: String) = repo.removeSessionListener(sessionId)

    /* ===== Personagens da sessão ===== */
    private val _sessionCharacters = MutableStateFlow<Map<String, List<MRCharacter>>>(emptyMap())
    val sessionCharacters: StateFlow<Map<String, List<MRCharacter>>> = _sessionCharacters

    fun startCharactersListener(sessionId: String) {
        repo.listenCharacters(sessionId) { updated -> _sessionCharacters.value = updated }
    }

    fun stopCharactersListener(sessionId: String) = repo.removeCharactersListener(sessionId)

    /* ===== Sair da sessão ===== */
    fun leaveSession(sessionId: String, callback: (Boolean, Exception?) -> Unit) {
        val uid = _myUid.value
        if (uid == null) {
            callback(false, IllegalStateException("UID ausente"))
            return
        }
        repo.leaveSession(sessionId, uid, callback)
    }

    /* ===== Util ===== */
    fun isMaster(): Boolean = _sessionState.value?.masterId == _myUid.value

    /* ===== HP / Mana (somente Mestre) ===== */
    fun setHp(sessionId: String, ownerUid: String, charId: Long, hp: Int, hpMax: Int) {
        if (!isMaster()) return
        val coerced = hp.coerceIn(0, hpMax)
        FirebaseDatabase.getInstance()
            .getReference("sessions/$sessionId/characters/$ownerUid/$charId/runtime/hp")
            .setValue(coerced)
    }

    fun setMana(sessionId: String, ownerUid: String, charId: Long, mana: Int, manaMax: Int = 20) {
        if (!isMaster()) return
        val coerced = mana.coerceIn(0, manaMax)
        FirebaseDatabase.getInstance()
            .getReference("sessions/$sessionId/characters/$ownerUid/$charId/runtime/mana")
            .setValue(coerced)
    }
}
