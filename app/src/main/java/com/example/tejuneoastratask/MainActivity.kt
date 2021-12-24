package com.example.tejuneoastratask

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.tejuneoastratask.databinding.ActivityMain2Binding
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main2)

        binding.button.setOnClickListener {
            ImageActivity.start(this)
        }

        binding.button2.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()

            dialog.setTitle("Do you want to save Image to gallery?")

            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Save") { _, _ ->
                bitmap?.let { it1 ->
                    val file = ImageHelper(this).saveImageToGallery(it1)
                    Toast.makeText(this, file.absolutePath, Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }

            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel") { _, _ ->
                dialog.dismiss()
            }

            dialog.show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == ImageActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    bitmap = data?.let { ImageActivity.getResultBitmap(it) }
                    binding.ImageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}