package com.example.chattitesti

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {
    private const val COLLECTION_NAME = "chat_messages"
    private val firestore = FirebaseFirestore.getInstance()

    fun sendMessage(message: Message, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection(COLLECTION_NAME)
            .add(message)
            .addOnSuccessListener { onSuccess.invoke() }
            .addOnFailureListener { e -> onFailure.invoke(e) }
    }
}