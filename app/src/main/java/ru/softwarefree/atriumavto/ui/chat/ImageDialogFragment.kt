package ru.softwarefree.atriumavto.ui.chat

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import ru.softwarefree.atriumavto.databinding.DialogImageBinding

class ImageDialogFragment : DialogFragment() {

    private var _binding: DialogImageBinding? = null
    private val binding get() = _binding!!

    private var imageBitmap: Bitmap? = null

    companion object {
        private const val ARG_IMAGE = "image"

        fun newInstance(imageBitmap: Bitmap): ImageDialogFragment {
            val fragment = ImageDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_IMAGE, imageBitmap)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageBitmap = arguments?.getParcelable(ARG_IMAGE)
        imageBitmap?.let {
            binding.photoView.setImage(ImageSource.bitmap(it))
            binding.photoView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
            binding.photoView.maxScale = 20f
            binding.photoView.minScale = 1f
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}