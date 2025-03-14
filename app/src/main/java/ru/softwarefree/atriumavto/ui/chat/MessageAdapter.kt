package ru.softwarefree.atriumavto.ui.chat

import android.graphics.Bitmap
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
    private val currentUserId: String,
    private val onImageClick: (Bitmap) -> Unit,
    private val onLikeClick: (messageId: String) -> Unit
) : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SELF = 1
        private const val VIEW_TYPE_OTHER = 2
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).senderId == currentUserId) VIEW_TYPE_SELF else VIEW_TYPE_OTHER

    override fun getItemId(position: Int): Long = getItem(position).id.hashCode().toLong()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == VIEW_TYPE_SELF) R.layout.item_message_self else R.layout.item_message
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position), onImageClick, onLikeClick)
    }

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val senderNameTextView: TextView = view.findViewById(R.id.senderNameTextView)
        private val messageTextView: TextView = view.findViewById(R.id.messageTextView)
        private val messageImageView: ImageView = view.findViewById(R.id.messageImageView)
        private val likeIcon: ImageView = view.findViewById(R.id.likeIcon)

        fun bind(
            message: Message,
            onImageClick: (Bitmap) -> Unit,
            onLikeClick: (String) -> Unit
        ) {
            senderNameTextView.text = message.senderName

            when {
                !message.imageUrl.isNullOrEmpty() -> {
                    if (messageImageView.tag != message.imageUrl) {
                        messageImageView.tag = message.imageUrl
                        Glide.with(itemView.context)
                            .load(message.imageUrl)
                            .into(messageImageView)
                    }
                    messageImageView.visibility = View.VISIBLE
                    messageTextView.visibility = View.GONE
                    messageImageView.setOnClickListener {
                        (messageImageView.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap?.let(onImageClick)
                    }
                }
                message.text.startsWith("emoji:") -> {
                    val emojiName = message.text.removePrefix("emoji:")
                    val resId = itemView.context.resources.getIdentifier(emojiName, "drawable", itemView.context.packageName)
                    if (resId != 0) {
                        messageImageView.setImageResource(resId)
                        messageImageView.visibility = View.VISIBLE
                        messageTextView.visibility = View.GONE
                        val sizeInPx = itemView.context.resources.getDimensionPixelSize(R.dimen.emoji_size)
                        messageImageView.layoutParams = messageImageView.layoutParams.apply {
                            width = sizeInPx
                            height = sizeInPx
                        }
                    }
                }
                else -> {
                    messageTextView.text = message.text
                    messageTextView.visibility = View.VISIBLE
                    messageImageView.visibility = View.GONE
                }
            }

            likeIcon.setImageResource(if (message.isLiked) R.drawable.ic_heart_outline else R.drawable.ic_heart_filled)
            likeIcon.setOnClickListener { onLikeClick(message.id) }
        }
    }
}