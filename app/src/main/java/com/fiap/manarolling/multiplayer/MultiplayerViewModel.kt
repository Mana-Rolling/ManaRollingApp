package com.fiap.manarolling.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MultiplayerViewModel : ViewModel() {

    private val repo = MultiplayerRepository()

    private val _sessionState = MutableStateFlow<GameSession?>(null)
    val sessionState: StateFlow<GameSession?> = _sessionState

    fun createSession(masterId: String, masterName: String, onResult: (String?, Exception?) -> Unit) {
        repo.createSession(masterId, masterName, onResult)
    }

    fun joinSession(sessionId: String, player: PlayerInfo, onResult: (Boolean, Exception?) -> Unit) {
        repo.joinSession(sessionId, player, onResult)
    }

    private var listener: ValueEventListener? = null

    fun startListening(sessionId: String) {
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val session = snapshot.getValue(GameSession::class.java)
                viewModelScope.launch {
                    _sessionState.value = session
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // log or handle error
            }
        }
        repo.listenSession(sessionId, listener!!)
    }

    fun stopListening(sessionId: String) {
        repo.removeListener(sessionId)
        listener = null
    }

    fun updateState(sessionId: String, state: Map<String, Any>, onComplete: ((Boolean) -> Unit)? = null) {
        repo.updateState(sessionId, state) { success, _ ->
            onComplete?.invoke(success)
        }
    }

    fun leaveSession(sessionId: String, playerId: String, onComplete: ((Boolean) -> Unit)? = null) {
        repo.leaveSession(sessionId, playerId) { success, _ -> onComplete?.invoke(success) }
    }
}
