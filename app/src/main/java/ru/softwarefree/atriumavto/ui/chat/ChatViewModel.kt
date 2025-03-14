package ru.softwarefree.atriumavto.ui.chat

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val _messages = MutableLiveData<List<Message>?>()
    val messages: MutableLiveData<List<Message>?> get() = _messages
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("messages")
    private val messageList = mutableListOf<Message>()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                message?.let {
                    messageList.add(it)
                    _messages.value = messageList.toList()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val updatedMessage = snapshot.getValue(Message::class.java)
                updatedMessage?.let { newMessage ->
                    val index = messageList.indexOfFirst { it.id == newMessage.id }
                    if (index != -1) {
                        messageList[index] = newMessage
                        _messages.value = messageList.toList()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun sendMessage(text: String) {
        val messageId = database.push().key ?: return
        val sharedPreferences = getApplication<Application>().getSharedPreferences("user_prefs", 0)
        val displayName = sharedPreferences.getString("displayName", "Unknown")

        val message = Message(
            id = messageId,
            text = text,
            senderId = getCurrentUserId(),
            senderName = displayName ?: "Unknown",
            timestamp = System.currentTimeMillis()
        )
        database.child(messageId).setValue(message)
    }

    private fun sendMessageWithImage(imageUrl: String) {
        val messageId = database.push().key ?: return
        val sharedPreferences = getApplication<Application>().getSharedPreferences("user_prefs", 0)
        val displayName = sharedPreferences.getString("displayName", "Unknown")
        val message = Message(
            id = messageId,
            text = "",
            imageUrl = imageUrl,
            senderId = getCurrentUserId(),
            senderName = displayName ?: "Unknown",
            timestamp = System.currentTimeMillis()
        )
        database.child(messageId).setValue(message)
    }

    fun uploadImageToFirebase(imageBitmap: Bitmap) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        imageRef.putBytes(data).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                sendMessageWithImage(uri.toString())
            }
        }.addOnFailureListener {
            // Обработка ошибок
        }
    }

    fun markMessagesAsRead() {
        val updatedMessages = messages.value?.map { message ->
            if (!message.isRead) {
                message.copy(isRead = true)
            } else {
                message
            }
        }
        _messages.value = updatedMessages
    }

    fun toggleLike(messageId: String) {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) return

        val messageRef = FirebaseDatabase.getInstance().reference.child("messages").child(messageId)

        messageRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                val message = snapshot.getValue(Message::class.java) ?: return
                val likedBy = message.likedBy?.toMutableMap() ?: mutableMapOf()

                if (likedBy.containsKey(userId)) {
                    likedBy.remove(userId)
                } else {
                    likedBy[userId] = true
                }

                messageRef.updateChildren(mapOf("likedBy" to likedBy))
            }

            override fun onCancelled(error: DatabaseError) {
                // Логируем ошибку
            }
        })
    }

    fun getCurrentUserId(): String {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    // Ошибка авторизации
                }
            }
            return ""
        }

        return user.uid
    }
}