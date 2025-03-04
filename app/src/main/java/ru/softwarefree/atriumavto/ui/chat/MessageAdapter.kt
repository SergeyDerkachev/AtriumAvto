package ru.softwarefree.atriumavto.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.softwarefree.atriumavto.R

class MessageAdapter(
    private val onImageClick: (bitmap: android.graphics.Bitmap) -> Unit
) : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val senderNameTextView: TextView = view.findViewById(R.id.senderNameTextView)
        val messageTextView: TextView = view.findViewById(R.id.messageTextView)
        val messageImageView: ImageView = view.findViewById(R.id.messageImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)

        holder.senderNameTextView.text = message.senderName

        if (!message.imageUrl.isNullOrEmpty()) {
            holder.apply {
                messageImageView.visibility = View.VISIBLE
                messageTextView.visibility = View.GONE

                if (messageImageView.tag == message.imageUrl) return

                messageImageView.tag = message.imageUrl

                Glide.with(itemView.context)
                    .load(message.imageUrl)
                    .into(messageImageView)

                messageImageView.setOnClickListener { onImageClick((messageImageView.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap ?: return@setOnClickListener) }
            }
        } else {
            holder.apply {
                messageTextView.visibility = View.VISIBLE
                messageImageView.visibility = View.GONE
                messageTextView.text = message.text
            }
        }
    }
}