package com.example.tejuneoastratask

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class ImageHelper(private val activity: Activity) {

    var intent: Intent? = null
    var resultCode = 0

    fun saveScaledPhotoToFile(photoBm: Bitmap, imageView: ImageView): Bitmap {
        var photoBm = photoBm
        val bmOriginalWidth = photoBm.width
        val bmOriginalHeight = photoBm.height
        val originalWidthToHeightRatio = 1.0 * bmOriginalWidth / bmOriginalHeight
        val originalHeightToWidthRatio = 1.0 * bmOriginalHeight / bmOriginalWidth
        //choose a maximum height
        val maxHeight = 1024
        //choose a max width
        val maxWidth = 1024
        //call the method to get the scaled bitmap
        photoBm = getScaledBitmap(
            photoBm, bmOriginalWidth, bmOriginalHeight,
            originalWidthToHeightRatio, originalHeightToWidthRatio,
            maxHeight, maxWidth
        )

        //create a byte array output stream to hold the photo's bytes
        val bytes = ByteArrayOutputStream()
        //compress the photo's bytes into the byte array output stream
        photoBm.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val byteArray = bytes.toByteArray()
        val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        imageView.setImageBitmap(bmp)
        Log.e("new sixe...", bytes.toByteArray().size.toString() + "..." + bmp.allocationByteCount)
        return bmp
    }

    fun saveImageToGallery(bitmap: Bitmap): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(storageDir, "$imageFileName.jpg")

        Log.e(TAG, "saveImageToGallery: ${imageFile.absolutePath}" )

        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Log.e("file", "Error writing bitmap", e)
        }

        return imageFile
    }

    fun convertBitmapToFile(bitmap: Bitmap): File {
        val filesDir = activity.filesDir
        val imageFile = File(filesDir, "newFileName" + ".jpg")
        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Log.e("file", "Error writing bitmap", e)
        }
        return imageFile
    }

    fun createFilePath(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var file: File? = null
        try {
            file = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpeg",  /* suffix */
                storageDir /* directory */
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.e(TAG, "createFilePath: filePath:" + file!!.absolutePath)
        return file
    }

    companion object {
        private const val TAG = "ImageHelper"
        private fun getScaledBitmap(
            bm: Bitmap,
            bmOriginalWidth: Int,
            bmOriginalHeight: Int,
            originalWidthToHeightRatio: Double,
            originalHeightToWidthRatio: Double,
            maxHeight: Int,
            maxWidth: Int
        ): Bitmap {
            var bm = bm
            if (bmOriginalWidth > maxWidth || bmOriginalHeight > maxHeight) {
                bm = if (bmOriginalWidth > bmOriginalHeight) {
                    scaleDeminsFromWidth(bm, maxWidth, bmOriginalHeight, originalHeightToWidthRatio)
                } else {
                    scaleDeminsFromHeight(
                        bm,
                        maxHeight,
                        bmOriginalHeight,
                        originalWidthToHeightRatio
                    )
                }
            }
            return bm
        }

        private fun scaleDeminsFromHeight(
            bm: Bitmap,
            maxHeight: Int,
            bmOriginalHeight: Int,
            originalWidthToHeightRatio: Double
        ): Bitmap {
            var bm = bm
            val newHeight =
                Math.min(maxHeight.toDouble(), bmOriginalHeight * .55).toInt()
            val newWidth = (newHeight * originalWidthToHeightRatio).toInt()
            bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true)
            return bm
        }

        private fun scaleDeminsFromWidth(
            bm: Bitmap,
            maxWidth: Int,
            bmOriginalWidth: Int,
            originalHeightToWidthRatio: Double
        ): Bitmap {
            //scale the width
            var bm = bm
            val newWidth =
                Math.min(maxWidth.toDouble(), bmOriginalWidth * .75).toInt()
            val newHeight = (newWidth * originalHeightToWidthRatio).toInt()
            bm = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true)
            return bm
        }
    }
}