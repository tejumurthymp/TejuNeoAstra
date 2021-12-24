import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tejuneoastratask.ImageActivity
import com.example.tejuneoastratask.R
import com.example.tejuneoastratask.databinding.PhotoBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PhotoBottomSheet(private val imageActivity: ImageActivity) : BottomSheetDialogFragment() {

    private var binding: PhotoBottomSheetBinding? = null
    private val TAG = "PhotoBottomSheet"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = PhotoBottomSheetBinding.inflate(inflater, container, false)

        binding!!.fabCamera.setOnClickListener {
            dismiss()
            imageActivity.takePhotoFromCamera(ImageActivity.CAMERA_CODE)
        }

        binding!!.fabGallery.setOnClickListener {
            dismiss()
            imageActivity.choosePhotoFromGallary(ImageActivity.GALLERY_CODE)
        }

        return binding!!.root
    }

}