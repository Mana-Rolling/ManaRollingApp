package com.fiap.manarolling.multiplayer

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MultiplayerRepository {

    private val database = FirebaseDatabase.getInstance()
    private val sessionsRef = database.getReference("sessions")

    private val listeners = mutableMapOf<String, ValueEventListener>()

    fun createSession(masterId: String, masterName: String, callback: (String?, Exception?) -> Unit) {
        val sessionId = generateSessionCode()
        val session = GameSession(id = sessionId, masterId = masterId, masterName = masterName, createdAt = System.currentTimeMillis())
        sessionsRef.child(sessionId).setValue(session).addOnCompleteListener { task ->
            if (task.isSuccessful) callback(sessionId, null) else callback(null, task.exception)
        }
    }

    fun joinSession(sessionId: String, player: PlayerInfo, callback: (Boolean, Exception?) -> Unit) {
        sessionsRef.child(sessionId).child("players").child(player.id).setValue(player).addOnCompleteListener { task ->
            callback(task.isSuccessful, task.exception)
        }
    }

    fun listenSession(sessionId: String, listener: ValueEventListener) {
        val ref = sessionsRef.child(sessionId)
        ref.addValueEventListener(listener)
        listeners[sessionId] = listener
    }

    fun removeListener(sessionId: String) {
        listeners[sessionId]?.let {
            sessionsRef.child(sessionId).removeEventListener(it)
            listeners.remove(sessionId)
        }
    }

    fun updateState(sessionId: String, newState: Map<String, Any>, callback: ((Boolean, Exception?) -> Unit)? = null) {
        sessionsRef.child(sessionId).child("state").setValue(newState).addOnCompleteListener { task ->
            callback?.invoke(task.isSuccessful, task.exception)
        }
    }

    fun leaveSession(sessionId: String, playerId: String, callback: ((Boolean, Exception?) -> Unit)? = null) {
        sessionsRef.child(sessionId).child("players").child(playerId).removeValue().addOnCompleteListener { task ->
            callback?.invoke(task.isSuccessful, task.exception)
        }
    }

    private fun generateSessionCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
