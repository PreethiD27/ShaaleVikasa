package com.shaalevikas.app.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun login(email: String, password: String): Result<UserRole> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user!!.uid
        val doc = db.collection("users").document(uid).get().await()
        doc.toObject(UserRole::class.java) ?: UserRole(uid, email, "alumni")
    }

    suspend fun register(email: String, password: String, role: String = "alumni"): Result<UserRole> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user!!.uid
        val user = UserRole(uid, email, role)
        db.collection("users").document(uid).set(user).await()
        user
    }

    fun logout() = auth.signOut()

    fun currentUserRole(): Flow<UserRole?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(null); close(); return@callbackFlow }
        val listener = db.collection("users").document(uid)
            .addSnapshotListener { snap, _ -> trySend(snap?.toObject(UserRole::class.java)) }
        awaitClose { listener.remove() }
    }

    fun getNeedsFlow(): Flow<List<Need>> = callbackFlow {
        val listener = db.collection("needs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(Need::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addNeed(need: Need): String {
        val ref = db.collection("needs").add(need).await()
        return ref.id
    }

    suspend fun updateNeed(need: Need) {
        db.collection("needs").document(need.id).set(need).await()
    }

    suspend fun deleteNeed(needId: String) {
        db.collection("needs").document(needId).delete().await()
    }

    fun getPledgesFlow(needId: String): Flow<List<Pledge>> = callbackFlow {
        val listener = db.collection("pledges")
            .whereEqualTo("needId", needId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(Pledge::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addPledge(pledge: Pledge) {
        db.collection("pledges").add(pledge).await()
        val needRef = db.collection("needs").document(pledge.needId)
        db.runTransaction { tx ->
            val snap = tx.get(needRef)
            val current = snap.getDouble("pledgedAmount") ?: 0.0
            tx.update(needRef, "pledgedAmount", current + pledge.amount)
        }.await()
    }

    suspend fun uploadPhoto(uri: Uri, path: String): String {
        val ref = storage.reference.child(path)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
