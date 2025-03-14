package ru.softwarefree.atriumavto.ui.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import ru.softwarefree.atriumavto.R
import ru.softwarefree.atriumavto.databinding.FragmentChatBinding
import java.io.File

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var photoUri: Uri
    private lateinit var cameraResultLauncher: ActivityResultLauncher<Intent>
    private var mediaPlayer: MediaPlayer? = null
    private var isUserScrolling = false
    private var isFirstLoad = true
    private lateinit var emojiPopup: PopupWindow

    private val emojiList = listOf(
        "angry_face",                    "anguished_face",             "anxious_face_with_sweat",    "astonished_face",            "backhand_index_pointing_down", "backhand_index_pointing_left",
        "backhand_index_pointing_right", "backhand_index_pointing_up", "beaming_face_with_smiling_eyes", "bell",                       "call_me_hand",               "check_mark",
        "clapping_hands",                "clown_face",                 "cold_face",                  "confounded_face",            "cowboy_hat_face",             "crossed_fingers",
        "crying_face",                   "dissapointed_face",          "dizzy_face",                 "downcast_face_with_sweat",   "drooling_face",              "exploding_head",
        "expressioless_face",            "face_blowing_a_kiss",        "face_savoring_food",         "face_screaming_in_fear",     "face_with_hand_over_mouth",  "face_with_head_bandage",
        "face_with_medical_mask",        "face_with_monocle",          "face_with_open_mouth",       "face_without_mouth",         "face_with_raised_eyebrow",   "face_with_rolling_eyes",
        "face_with_steam_from_nose",     "face_with_symbols_on_mouth", "face_with_tears_of_joy",     "face_with_thermometer",      "face_with_tongue",           "fearful_face",
        "fire",                          "flushed_face",               "folded_hands",               "frowning_face",              "frowning_face_with_open_mouth", "grimacing_face",
        "grinning_face",                 "grinning_face_1",            "grinning_face_with_big_eyes","grinning_face_with_sweat",   "grinning_squinting_face",    "handshake",
        "hand_with_fingers_splayed",     "heart",                      "hot_face",                   "hugging_face",               "hushed_face",                "index_pointing_up",
        "kissing_face",                  "kissing_face_with_closed_eyes", "kissing_face_with_smiling_eyes", "left_facing_fist",          "loudly_crying_face",         "love_you_gesture",
        "lying_face",                    "middle_finger",              "money_mouth_face",           "nauseated_face",             "nerd_face",                  "neutral_face",
        "ok_hand",                       "oncoming_fist",              "open_hands",                 "paised_back_of_hand",        "palms_up_together",          "partying_face",
        "pensive_face",                  "pile_of_poo",                "pinched_fingers",            "pinch_hand",                 "pleading_face",              "pouting_face",
        "raised_fist",                   "raised_hand",                "raising_hands",              "relieved_face",              "right_facing_fist",         "rolling_on_the_floor_laughing",
        "sad_but_relieved_face",         "santa_claus",                "scrunched_face",             "shushing_face",              "sign_of_the_horns",          "sleep_face",
        "sleepy_face",                   "slightly_frowning_face",     "slightly_smiling_face",      "smiling_face",               "smiling_face_with_halo",     "smiling_face_with_heart_eyes",
        "smiling_face_with_hearts",      "smiling_face_with_smiling_eyes", "smiling_face_with_sunglasses", "smirking_face",             "sneezing_face",              "squinting_face_with_tongue",
        "star_struck",                   "thinking_face",              "thumbs_down",                "thumbs_up",                  "tired_face",                 "unamused_face",
        "upside_down_face",              "victory_hand",               "vomiting_face",              "vulcan_salute",              "waving_hand",                "weary_face",
        "winking_face",                  "winking_face_with_tongue",   "woozy_face",                 "worried_face",               "writing_hand",               "yawning_face",
        "zany_face",                     "zipper_mouth_face"
        )

    private val messageAdapter by lazy {
        MessageAdapter(
            currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
            onImageClick = { bitmap ->
                ImageDialogFragment.newInstance(bitmap)
                    .show(childFragmentManager, "image_dialog")
            },
            onLikeClick = { messageId ->
                chatViewModel.toggleLike(messageId)
            }
        )
    }

    private fun setupEmojiPopup() {
        val emojiView = layoutInflater.inflate(R.layout.emoji_panel, null)
        val gridView: GridView = emojiView.findViewById(R.id.emojiGridView)

        val adapter = EmojiAdapter(requireContext(), emojiList) { emojiName ->
            chatViewModel.sendMessage("emoji:$emojiName") // Отправляем специальный формат
            emojiPopup.dismiss()
        }


        gridView.adapter = adapter

        emojiPopup = PopupWindow(emojiView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
    }

    private fun showEmojiPopup() {
        if (::emojiPopup.isInitialized.not()) setupEmojiPopup()
        emojiPopup.showAtLocation(binding.root, Gravity.BOTTOM, 0, 0)
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("user_prefs", 0).apply {
            getString("displayName", null)
                ?: findNavController().navigate(R.id.setupProfileFragment)
        }

        setupViews()
        setupObservers()
        setupCameraResultLauncher()
    }

    private fun setupViews() = binding.apply {
        recyclerView.setItemViewCacheSize(30)
        recyclerView.recycledViewPool.setMaxRecycledViews(0, 20)

        recyclerView.apply {
            adapter = messageAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.message_space)))

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy < 0) isUserScrolling = true
                }
            })
        }

        emojiButton.setOnClickListener {
            showEmojiPopup()
        }

        sendButton.setOnClickListener {
            messageEditText.text.toString().takeIf { it.isNotEmpty() }?.let {
                chatViewModel.sendMessage(it)
                messageEditText.text.clear()
                scrollToBottom()
            }
        }

        cameraButton.setOnClickListener {
            checkCameraPermissionAndLaunch()
            scrollToBottom()
        }
    }

    private fun setupObservers() {
        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            val messageList = messages ?: return@observe

            val lastMessage = messageList.lastOrNull()
            val isMessageFromCurrentUser = lastMessage?.senderId == chatViewModel.getCurrentUserId()

            if (lastMessage != null && !isMessageFromCurrentUser) {
                playNotificationSound()
            }

            messageAdapter.submitList(messageList) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val layoutManager = binding.recyclerView.layoutManager as? LinearLayoutManager
                    val shouldScrollToEnd =
                        !isUserScrolling && (isMessageFromCurrentUser || isFirstLoad)

                    if (layoutManager != null && shouldScrollToEnd) {
                        binding.recyclerView.scrollToPosition(messageList.size - 1)
                    }
                    chatViewModel.markMessagesAsRead()
                }
            }
        }
    }

    private fun scrollToBottom() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    private fun setupCameraResultLauncher() {
        cameraResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    uriToBitmap(photoUri)?.let { chatViewModel.uploadImageToFirebase(it) }
                }
            }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            requireContext().contentResolver.openInputStream(uri)
                ?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) launchCamera()
            else Toast.makeText(
                requireContext(),
                "Camera permission is required to take photos",
                Toast.LENGTH_SHORT
            ).show()
        }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> launchCamera()

            else -> requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoFile = File.createTempFile("IMG_", ".jpg", requireContext().cacheDir)
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )

        cameraResultLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        })
    }

    private fun playNotificationSound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.zvonok).apply {
            setOnCompletionListener { release() }
            start()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permission is required to take photos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    class SpaceItemDecoration(private val verticalSpaceHeight: Int) :
        RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
                outRect.bottom = verticalSpaceHeight
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
    }
}