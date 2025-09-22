package com.fiap.manarolling.multiplayer

import com.fiap.manarolling.model.Character as MRCharacter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
                    if (snap.exists()) {
                        // colisão rara de código: tenta outro
                        tryCreate(); return
                    }
                    val session = GameSession(
                        id = sessionId,
                        masterId = masterId,
                        masterName = masterName,
                        players = emptyMap(),
                        state = emptyMap(),
                        createdAt = System.currentTimeMillis()
                    )
                    sessionsRef.child(sessionId).setValue(session)
                        .addOnSuccessListener { callback(sessionId, null) }
                        .addOnFailureListener { callback(null, it) }
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(null, error.toException())
                }
            })
        }
        tryCreate()
    }

    /* ===== Entrar na sessão com personagem ===== */
    fun joinSessionWithCharacter(
        sessionId: String,
        playerId: String,
        playerName: String,
        character: MRCharacter,
        callback: (Boolean, Exception?) -> Unit
    ) {
        // garante ownerUid correto para a sessão (compatível com o resto do app)
        val sessionChar = try {
            character.copy(ownerUid = playerId)
        } catch (_: Throwable) {
            // se estiver numa versão antiga onde o copy falhar, usa o objeto como está
            character
        }

        val updates = hashMapOf<String, Any?>(
            "players/$playerId" to PlayerInfo(
                id = playerId,
                name = playerName,
                online = true,
                lastSeen = System.currentTimeMillis(),
                selectedCharacterId = character.id
            ),
            "characters/$playerId/${character.id}" to sessionChar
        )

        sessionsRef.child(sessionId).updateChildren(updates)
            .addOnSuccessListener { callback(true, null) }
            .addOnFailureListener { callback(false, it) }
    }

    /* ===== Estado da sessão ===== */
    fun startListening(sessionId: String, onUpdate: (GameSession?) -> Unit) {
        val l = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val s = snap.getValue(GameSession::class.java)
                onUpdate(s)
            }
            override fun onCancelled(error: DatabaseError) {
                onUpdate(null)
            }
        }
        sessionsRef.child(sessionId).addValueEventListener(l)
        listeners["session:$sessionId"] = l
    }

    fun removeSessionListener(sessionId: String) {
        listeners.remove("session:$sessionId")?.let { sessionsRef.child(sessionId).removeEventListener(it) }
    }

    /* ===== Personagens na sessão ===== */
    private fun charsRef(sessionId: String) = sessionsRef.child(sessionId).child("characters")

    fun listenCharacters(sessionId: String, onUpdate: (Map<String, List<MRCharacter>>) -> Unit) {
        val l = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val map = mutableMapOf<String, MutableList<MRCharacter>>()
                for (owner in snap.children) {
                    val ownerId = owner.key ?: continue
                    val list = mutableListOf<MRCharacter>()
                    for (c in owner.children) {
                        val ch = c.getValue(MRCharacter::class.java)
                        if (ch != null) list += ch
                    }
                    map[ownerId] = list
                }
                onUpdate(map)
            }
            override fun onCancelled(error: DatabaseError) {
                onUpdate(emptyMap())
            }
        }
        charsRef(sessionId).addValueEventListener(l)
        listeners["chars:$sessionId"] = l
    }

    fun removeCharactersListener(sessionId: String) {
        listeners.remove("chars:$sessionId")?.let { charsRef(sessionId).removeEventListener(it) }
    }

    /* ===== Atualizações de HP/Mana (apenas Mestre deve chamar) ===== */
    fun updateCharacterHp(
        sessionId: String,
        ownerUid: String,
        charId: Long,
        newHp: Int,
        hpMax: Int,
        callback: ((Boolean, Exception?) -> Unit)? = null
    ) {
        val coerced = newHp.coerceIn(0, hpMax)
        sessionsRef.child(sessionId)
            .child("characters").child(ownerUid).child(charId.toString())
            .child("runtime").child("hp")
            .setValue(coerced)
            .addOnSuccessListener { callback?.invoke(true, null) }
            .addOnFailureListener { e -> callback?.invoke(false, e) }
    }

    fun updateCharacterMana(
        sessionId: String,
        ownerUid: String,
        charId: Long,
        newMana: Int,
        manaMax: Int,
        callback: ((Boolean, Exception?) -> Unit)? = null
    ) {
        val coerced = newMana.coerceIn(0, manaMax)
        sessionsRef.child(sessionId)
            .child("characters").child(ownerUid).child(charId.toString())
            .child("runtime").child("mana")
            .setValue(coerced)
            .addOnSuccessListener { callback?.invoke(true, null) }
            .addOnFailureListener { e -> callback?.invoke(false, e) }
    }

    /* ===== Sair da sessão ===== */
    fun leaveSession(sessionId: String, playerId: String, callback: (Boolean, Exception?) -> Unit) {
        sessionsRef.child(sessionId).child("players").child(playerId)
            .removeValue().addOnCompleteListener { callback(it.isSuccessful, it.exception) }
    }
}
