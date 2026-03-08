package com.adrian.queuenote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("processes")

    // Guardar o actualizar un proceso
    suspend fun saveProcess(item: ProcessItem) {
        try {
            collection.document(item.id).set(item).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Obtener todos los procesos en tiempo real
    fun getProcessesFlow(): Flow<List<ProcessItem>> = callbackFlow {
        val subscription = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.toObjects(ProcessItem::class.java)
                trySend(items)
            }
        }
        awaitClose { subscription.remove() }
    }

    // Eliminar un proceso
    suspend fun deleteProcess(id: String) {
        try {
            collection.document(id).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
