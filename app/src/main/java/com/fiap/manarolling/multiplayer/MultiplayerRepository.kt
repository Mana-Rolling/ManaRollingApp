package com.fiap.manarolling.multiplayer

import com.fiap.manarolling.model.Character as MRCharacter
import com.google.firebase.database.*

class MultiplayerRepository(
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val sessionsRef = db.getReference("sessions")
    private val listeners = mutableMapOf<String, ValueEventListener>()

    /* ===== Criação de sessão ===== */
    fun createSession(masterId: String, masterName: String, callback: (String?, Exception?) -> Unit) {
        fun code() = (1..6).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")
        fun tryCreate() {
            val sessionId = code()
            sessionsRef.child(sessionId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    if (snap.exists()) return tryCreate()
                    val session = GameSession(
                        id = sessionId,
                        masterId = masterId,
                        masterName = masterName,
                        players = mapOf(masterId to PlayerInfo(id = masterId, name = masterName, online = true, lastSeen = System.currentTimeMillis())),
                        state = emptyMap(),
                        createdAt = System.currentTimeMillis()
                    )
                    sessionsRef.child(sessionId).setValue(session).addOnCompleteListener {
                        callback(if (it.isSuccessful) sessionId else null, it.exception)
                    }
                }
                override fun onCancelled(error: DatabaseError) = callback(null, error.toException())
            })
        }
        tryCreate()
    }

    /* ===== Join já levando um personagem (atômico) ===== */
    fun joinSessionWithCharacter(
        sessionId: String,
        uid: String,
        playerName: String,
        character: MRCharacter,
        callback: (Boolean, Exception?) -> Unit
    ) {
        val updates = hashMapOf<String, Any>(
            "/sessions/$sessionId/players/$uid" to PlayerInfo(
                id = uid,
                name = playerName,
                online = true,
                lastSeen = System.currentTimeMillis(),
                selectedCharacterId = character.id
            ),
            "/sessions/$sessionId/characters/$uid/${character.id}" to character
        )
        db.reference.updateChildren(updates).addOnCompleteListener { callback(it.isSuccessful, it.exception) }
    }

    /* ===== Listeners de sessão ===== */
    fun startListening(sessionId: String, onUpdate: (GameSession?) -> Unit) {
        removeSessionListener(sessionId)
        val l = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onUpdate(snapshot.getValue(GameSession::class.java))
            }
            override fun onCancelled(error: DatabaseError) { onUpdate(null) }
        }
        sessionsRef.child(sessionId).addValueEventListener(l)
        listeners["session:$sessionId"] = l
    }
    fun removeSessionListener(sessionId: String) {
        listeners.remove("session:$sessionId")?.let {
            sessionsRef.child(sessionId).removeEventListener(it)
        }
    }

    /* ===== Personagens na sessão ===== */
    private fun charsRef(sessionId: String) = sessionsRef.child(sessionId).child("characters")

    fun listenCharacters(sessionId: String, onUpdate: (Map<String, List<MRCharacter>>) -> Unit) {
        removeCharactersListener(sessionId)
        val l = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val map = mutableMapOf<String, MutableList<MRCharacter>>()
                snapshot.children.forEach { owner ->
                    val list = mutableListOf<MRCharacter>()
                    owner.children.forEach { c -> c.getValue(MRCharacter::class.java)?.let(list::add) }
                    map[owner.key ?: ""] = list
                }
                onUpdate(map)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        charsRef(sessionId).addValueEventListener(l)
        listeners["chars:$sessionId"] = l
    }
    fun removeCharactersListener(sessionId: String) {
        listeners.remove("chars:$sessionId")?.let { charsRef(sessionId).removeEventListener(it) }
    }

    /* ===== Util ===== */
    fun leaveSession(sessionId: String, playerId: String, callback: (Boolean, Exception?) -> Unit) {
        sessionsRef.child(sessionId).child("players").child(playerId)
            .removeValue().addOnCompleteListener { callback(it.isSuccessful, it.exception) }
    }
}
