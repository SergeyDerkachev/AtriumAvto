package ru.softwarefree.atriumavto.ui.chat

data class Message(
    val id: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val senderId: String = "",
    val timestamp: Long = 0,
    val senderName: String ="",
    val isRead: Boolean = false
)