package ru.softwarefree.atriumavto.ui.chat

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class EmojiAdapter(
    private val context: Context,
    private val emojiResIds: List<String>,
    private val onEmojiSelected: (String) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = emojiResIds.size

    override fun getItem(position: Int): Any = emojiResIds[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val emojiName = emojiResIds[position]

        val imageView = convertView as? ImageView ?: ImageView(context).apply {
            layoutParams = ViewGroup.LayoutParams(120, 120)
            setPadding(8, 8, 8, 8)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }

        val resId = context.resources.getIdentifier(emojiName, "drawable", context.packageName)

        if (resId != 0) {
            imageView.setImageResource(resId)
        } else {
            imageView.setImageResource(android.R.color.transparent)
        }

        imageView.setOnClickListener { onEmojiSelected(emojiName) }

        return imageView
    }
}