package ru.softwarefree.atriumavto.ui.chat

import com.google.firebase.auth.FirebaseAuth

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0L,
    val isRead: Boolean = false,
    val likedBy: Map<String, Boolean>? = null
) {
    val isLiked: Boolean
        get() = likedBy?.containsKey(FirebaseAuth.getInstance().currentUser?.uid) == true
}