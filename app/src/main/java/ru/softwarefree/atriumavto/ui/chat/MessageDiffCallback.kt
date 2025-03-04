package ru.softwarefree.atriumavto.ui.chat

import androidx.recyclerview.widget.DiffUtil

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.text == newItem.text &&
                oldItem.imageUrl == newItem.imageUrl &&
                oldItem.timestamp == newItem.timestamp &&
                oldItem.isRead == newItem.isRead
    }

    override fun getChangePayload(oldItem: Message, newItem: Message): Any? {
        val diffBundle = mutableMapOf<String, Any?>()

        if (oldItem.text != newItem.text) {
            diffBundle["text"] = newItem.text
        }
        if (oldItem.imageUrl != newItem.imageUrl) {
            diffBundle["imageUrl"] = newItem.imageUrl
        }
        if (oldItem.timestamp != newItem.timestamp) {
            diffBundle["timestamp"] = newItem.timestamp
        }
        if (oldItem.isRead != newItem.isRead) {
            diffBundle["isRead"] = newItem.isRead
        }

        return diffBundle.ifEmpty { null }
    }
}