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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private val messageAdapter by lazy {
        MessageAdapter { imageBitmap ->
            ImageDialogFragment.newInstance(imageBitmap)
                .show(childFragmentManager, "image_dialog")
        }
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

        sharedPreferences = requireContext().getSharedPreferences("user_prefs", 0)
        sharedPreferences.getString("displayName", null) ?: findNavController().navigate(R.id.setupProfileFragment)

        setupViews()
        setupObservers()
        setupCameraResultLauncher()
    }

    private fun setupViews() = binding.apply {
        recyclerView.apply {
            adapter = messageAdapter
            setHasFixedSize(true)
            addItemDecoration(SpaceItemDecoration(resources.getDimensionPixelSize(R.dimen.message_space)))

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    isUserScrolling = dy < 0
                }
            })
        }

        sendButton.setOnClickListener {
            messageEditText.text.toString().takeIf { it.isNotEmpty() }?.let {
                chatViewModel.sendMessage(it)
                messageEditText.text.clear()
            }
        }

        cameraButton.setOnClickListener { checkCameraPermissionAndLaunch() }
    }

    private fun setupObservers() {
        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            val messageList = messages ?: return@observe
            messageAdapter.submitList(messageList) {
                binding.recyclerView.post {
                    val layoutManager = binding.recyclerView.layoutManager as? LinearLayoutManager
                    if (!isUserScrolling && layoutManager != null) {
                        val lastUnreadMessageIndex = messageList.indexOfLast { !it.isRead }
                        binding.recyclerView.smoothScrollToPosition(
                            if (lastUnreadMessageIndex != -1) lastUnreadMessageIndex else messageList.size - 1
                        )
                    }
                    chatViewModel.markMessagesAsRead()
                }
            }
        }
    }

    private fun setupCameraResultLauncher() {
        cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                uriToBitmap(photoUri)?.let { chatViewModel.uploadImageToFirebase(it) }
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun launchCamera() {
        val photoFile = File.createTempFile("IMG_", ".jpg", requireContext().cacheDir)
        photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)

        cameraResultLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        })
    }

    private fun playNotificationSound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.zvonok).apply { start() }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    class SpaceItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
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