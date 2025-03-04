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
                    _messages.value = messageList
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun sendMessage(text: String) {
        val messageId = database.push().key ?: return
        val user = FirebaseAuth.getInstance().currentUser
        val sharedPreferences = getApplication<Application>().getSharedPreferences("user_prefs", 0)
        val displayName = sharedPreferences.getString("displayName", "Unknown")

        val message = Message(
            id = messageId,
            text = text,
            senderId = user?.uid ?: "",
            senderName = displayName ?: "Unknown",
            timestamp = System.currentTimeMillis()
        )
        database.child(messageId).setValue(message)
    }

    private fun sendMessageWithImage(imageUrl: String) {
        val messageId = database.push().key ?: return
        val user = FirebaseAuth.getInstance().currentUser
        val sharedPreferences = getApplication<Application>().getSharedPreferences("user_prefs", 0)
        val displayName = sharedPreferences.getString("displayName", "Unknown")
        val message = Message(
            id = messageId,
            text = "",
            imageUrl = imageUrl,
            senderId = user?.uid ?: "",
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
}