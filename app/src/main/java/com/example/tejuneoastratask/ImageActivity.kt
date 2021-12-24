package com.example.tejuneoastratask

import PhotoBottomSheet
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.example.tejuneoastratask.databinding.ActivityMainBinding
import java.io.File


class ImageActivity : AppCompatActivity() {

    private var imageHelper: ImageHelper? = null
    private var photoURI: Uri? = null
    private var rotate = 0
    private var bitmap: Bitmap? = null
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "ImageHelper2Activity"
        const val CAMERA_CODE = 9090
        const val GALLERY_CODE = 7070
        const val REQUEST_CODE = 1997
        const val RESULT_FILE = "result_bitmap"

        @JvmStatic
        fun start(activity: Activity) {
            activity.startActivityForResult(Intent(activity, ImageActivity::class.java), REQUEST_CODE)
        }

        @JvmStatic
        fun getResultBitmap(intent: Intent): Bitmap? {
            val file: File = intent.extras?.get(RESULT_FILE) as File
            return BitmapFactory.decodeFile(file.absolutePath)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        supportActionBar?.title = "CHOOSE PHOTO"
        imageHelper = ImageHelper(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestCameraPermission()
            finish()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            requestReadPermission()
            finish()
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            requestWritePermission()
            finish()
        }

        bottomSheet()

        binding.floatingActionButton.setOnClickListener {
            binding.floatingActionButton.visibility = GONE
            Intent().let {
                val file = bitmap?.let { it1 -> imageHelper!!.convertBitmapToFile(it1) }
                it.putExtra(RESULT_FILE, file)
                setResult(RESULT_OK, it)
                finish()
            }
        }
    }

    private fun bottomSheet() {
        PhotoBottomSheet(this).apply {
            Log.e(TAG, "bottomSheet: called" )
            binding.imgview.visibility = GONE
            binding.floatingActionButton.visibility = GONE
            show(supportFragmentManager, this.tag)
        }
    }

    fun choosePhotoFromGallary(code: Int) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2000)
        }else{
            startGallery(code)
        }
    }

    fun takePhotoFromCamera(code: Int) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if ((ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 2000)
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2000)
        } else{
            startCamera(code)
        }
    }


    private fun startGallery(code: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        if (intent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(Intent.createChooser(intent, "Select File"), code)
        }
    }

    private fun startCamera(code: Int) {
        Log.e(TAG, "startCamera: called" )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoURI = imageHelper!!.createFilePath()?.let {
            FileProvider.getUriForFile(this,
                "com.example.tejuneoastratask.fileprovider",
                it
            )
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        Log.e(TAG, "startCamera: $photoURI" )
        startActivityForResult(intent, code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.e(TAG, "onActivityResult: requestcode:$requestCode , resultCode:$resultCode")
        when (requestCode) {
            GALLERY_CODE -> {
                if (resultCode == RESULT_OK) {
                    binding.imgview.visibility = VISIBLE
                    data?.apply {
                        photoURI = this.data
                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, this.data)
                        binding.imgview.setImageBitmap(bitmap)
                        binding.floatingActionButton.visibility = VISIBLE
                    }
                    Log.e(TAG, "onActivityResult: ${binding.imgview.isVisible}" )
                } else finish()
            }

            CAMERA_CODE -> {
                if (resultCode == RESULT_OK) {
                    binding.imgview.visibility = VISIBLE
                    bitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoURI)
                    binding.imgview.setImageBitmap(bitmap)
                    binding.floatingActionButton.visibility = VISIBLE
                    Log.e(TAG, "onActivityResult: ${binding.imgview.isVisible}" )
                } else finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.image_crop, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.crop -> bitmap?.let { performCrop(it) }
            R.id.cancel -> bottomSheet()
            R.id.rotate -> bitmap?.let { rotateImage(it) }
            R.id.vertical_flip -> bitmap?.let { verticalFlip(it) }
            R.id.horizontal_flip -> bitmap?.let { horizontalFlip(it) }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun horizontalFlip(bitmapImage: Bitmap) {
        Matrix().let {
            it.preScale(-1.0f, 1.0f)
            bitmap = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.width, bitmapImage.height, it, true) // rotating bitmap
            binding.imgview.setImageBitmap(bitmap)
        }
    }

    private fun verticalFlip(bitmapImage: Bitmap) {
        Matrix().let {
            it.preScale(1.0f, -1.0f)
            bitmap = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.width, bitmapImage.height, it, true) // rotating bitmap
            binding.imgview.setImageBitmap(bitmap)
        }
    }

    private fun rotateImage(bitmapImage: Bitmap) {
        rotate = 90

        Matrix().let {
            it.postRotate(rotate.toFloat())
            bitmap = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.width, bitmapImage.height, it, true) // rotating bitmap
            binding.imgview.setImageBitmap(bitmap)
        }
    }

    private fun performCrop(bitmapImage: Bitmap) {
        if (bitmapImage.width >= bitmapImage.height) {
            bitmap = Bitmap.createBitmap(
                bitmapImage,
                bitmapImage.width / 2 - bitmapImage.height / 2,
                0,
                bitmapImage.height,
                bitmapImage.height
            )
        } else {
            bitmap = Bitmap.createBitmap(
                bitmapImage,
                0,
                bitmapImage.height / 2 - bitmapImage.width / 2,
                bitmapImage.width,
                bitmapImage.width
            )
        }
        binding.imgview.setImageBitmap(bitmap)
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 2000)
    }

    private fun requestWritePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),2000)
    }

    private fun requestReadPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2000)
    }

}
