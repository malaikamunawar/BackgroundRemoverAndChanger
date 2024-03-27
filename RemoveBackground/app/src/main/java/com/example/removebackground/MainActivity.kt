package com.example.removebackground

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var imgview: ImageView
    private lateinit var rbgbutton: Button
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rbgbutton = findViewById(R.id.button)
        imgview = findViewById(R.id.imageView3)


        rbgbutton.setOnClickListener {
         removeBackground()
        }

    }
    // Function to select an image from the gallery
    fun selectImageFromGallery(view: View) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    // Handle the result of the gallery image selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Image selected from the gallery, save the URI
                selectedImageUri = uri
                val imageView = findViewById<ImageView>(R.id.imageView3)
                imageView.visibility = View.VISIBLE
                imageView.setImageURI(uri)
            }
        }
    }

    public fun removeBackground(): String {
        val apiKey = "Insert key here"
        val url = "https://api.remove.bg/v1.0/removebg"
        val selectedUri = selectedImageUri

        var outputfilepath = ""
        // Ensure an image is selected from the gallery
        if (selectedUri == null) {
           // Log.e("Error", "No image selected from the gallery.")
            Toast.makeText(applicationContext, "Select an image first.", Toast.LENGTH_SHORT).show()
            //return
        }

        // Get the image file path from the selected image URI
        val imageFilePath = selectedUri?.let { getImageFilePathFromUri(it) }

        if (imageFilePath != null) {
            val imageRequestBody = applicationContext.contentResolver.openInputStream(selectedUri)
                ?.use { inputStream ->
                    inputStream.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
                } ?: "".toRequestBody("image/jpeg".toMediaTypeOrNull())

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .addHeader("X-Api-Key", apiKey)
                .post(
                    MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image_file", "image.jpg", imageRequestBody)
                        .addFormDataPart("size", "auto")
                        .build()
                )
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle the failure
                    Log.d("onFailure", e.message.toString())
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body
                        Log.d("Success", responseBody.toString())
                        if (responseBody != null) {
                            val inputStream = responseBody.byteStream()
                            val outputFile = File(applicationContext.filesDir, "no-bg.png")
                            outputFile.outputStream().use { fileOutputStream ->
                                inputStream.copyTo(fileOutputStream)
                            }
                            println("Background removed image saved successfully.")
//                            runOnUiThread {
//                                val bitmap = BitmapFactory.decodeFile(outputFile.path)
//                                imgview.setImageBitmap(bitmap)
//                                bgbutton.setOnClickListener {
                                    outputfilepath= outputFile.path
//                                    startBgActivity(outputfilepath)
//                                    Log.d("ADebugTag", "Value: " + outputfilepath);
//
//                                }
//                                var isImageDisplayed = true
//                                if(isImageDisplayed==true){
//                                    rbgbutton.setVisibility(View.GONE)
//                                    savebtn.setVisibility(View.VISIBLE)
//                                    bgbutton.setVisibility(View.VISIBLE)
//                                }
//                            }

                            startBgActivity(outputfilepath)
                            Log.d("ADebugTag", "Value: " + outputfilepath);
                        } else {
                            println("Response body is null.")
                        }

                    } else {
                        Log.d("onFailure", response.message.toString())
                        println("Request failed with code: ${response.code}")
                    }
                    response.close()
                }
            })
        } else {
            Log.e("Error", "Failed to get image file path from the selected image.")
        }
        return outputfilepath
    }

    // Helper function to get the image file path from the URI



    private fun saveImageToGallery() {
        //val imageView = findViewById<ImageView>(R.id.imageView3)
        val drawable = imgview.drawable

        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val resolver = applicationContext.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "imagetest.png")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            imageUri?.let { uri ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    Toast.makeText(applicationContext, "Image saved to gallery.", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(applicationContext, "Failed to save image.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(applicationContext, "No image to save.", Toast.LENGTH_SHORT).show()
        }
    }


    public fun startBgActivity(imagePath: String) {
        val intent = Intent(this, bg::class.java)
        intent.putExtra("imagePath", imagePath)
        startActivity(intent)
    }
    private fun getImageFilePathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            return it.getString(columnIndex)
        }
        return null
    }
//    private fun removeBackground() {
//
//        val apiKey = "yaAYQZPhpdzS966YES22p8qn"
////        val url = "https://sdk.photoroom.com/v1/segment"
//      val url = "https://api.remove.bg/v1.0/removebg"
//        val imageFilePath = "@drawable/icon.jpg"
//
//        val client = OkHttpClient()
//        val request = Request.Builder()
//            .url(url)
//            .addHeader("X-Api-Key", apiKey)
//            .post(
//                MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addFormDataPart(
//                        "image_file",
//                        imageFilePath,
//                        applicationContext.resources.openRawResource(R.raw.car)
//                            .use { inputStream ->
//                                inputStream.readBytes().toRequestBody("image/jpeg".toMediaTypeOrNull())
//                            }
//                    )
//                    .addFormDataPart("size", "auto")
//                    .build()
//            )
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                // Handle the failure
//                Log.d("onFailure", e.message.toString())
//                e.printStackTrace()
//                alertDialog()
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) {
//                    val responseBody = response.body
//                    Log.d("Success", responseBody.toString())
//                    if (responseBody != null) {
//                        val inputStream = responseBody.byteStream()
//                        val outputFile = File(applicationContext.filesDir, "no-bg.png")
//                        outputFile.outputStream().use { fileOutputStream ->
//                            inputStream.copyTo(fileOutputStream)
//                        }
//                        println("Background removed image saved successfully.")
//                        runOnUiThread {
//                            val imageView = findViewById<ImageView>(R.id.imageView3)
//                            val bitmap = BitmapFactory.decodeFile(outputFile.path)
//                            imageView.setImageBitmap(bitmap)
//                        }
//                        savebtn.setOnClickListener {
//                            alertDialog()
//                        }
//                    } else {
//                        println("Response body is null.")
//                    }
//                } else {
//                    Log.d("onFailure", response.message.toString())
//                    println("Request failed with code: ${response.code}")
//                }
//                response.close()
//            }
//
//
//            public fun alertDialog() {
//                val builder = AlertDialog.Builder(this@MainActivity)
//                builder.setTitle("Androidly Alert")
//                builder.setMessage("We have a message")
//
//                builder.setPositiveButton(android.R.string.ok) { dialog, which ->
//                }
//                builder.show()
//            }
//
//
//        })
//    }
}

            //       override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) {
//                    val resolver = applicationContext.contentResolver
//
//                    // Save the processed image using MediaStore
//                    val contentValues = ContentValues().apply {
//                        put(MediaStore.MediaColumns.DISPLAY_NAME, "no-bg.png")
//                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
//                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
//                    }
//
//                    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//                    imageUri?.let { uri ->
//                        resolver.openOutputStream(uri)?.use { outputStream ->
//                            // Save the processed image to the OutputStream
//                            val outputFile = File(applicationContext.filesDir, "no-bg.png")
//                            outputFile.inputStream().use { inputStream ->
//                                inputStream.copyTo(outputStream)
//                            }
//                            runOnUiThread {
//                            val imageView = findViewById<ImageView>(R.id.imageView3)
//                            val bitmap = BitmapFactory.decodeFile(outputFile.path)
//                            imageView.setImageBitmap(bitmap)
//                        }
//
//                            println("Background removed image saved successfully.")
//                        }
//                    } ?: run {
//                        println("Failed to create MediaStore entry.")
//                    }
//
//                }
//                else {
//                    Log.d("onFailure", response.message)
//                    println("Request failed with code: ${response.code}")
//                }
//                response.close()
//
//            }
//        })

//            Toast.makeText(applicationContext,
//                android.R.string.ok, Toast.LENGTH_SHORT).show()


//public fun save() {
//
//    val resolver = applicationContext.contentResolver
//
//    // Save the processed image using MediaStore
//    val contentValues = ContentValues().apply {
//        put(MediaStore.MediaColumns.DISPLAY_NAME, "no-bg.png")
//        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
//        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
//    }
//
//    val imageUri =
//        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//    imageUri?.let { uri ->
//        resolver.openOutputStream(uri)?.use { outputStream ->
//            // Save the processed image to the OutputStream
//            val outputFile = File(applicationContext.filesDir, "no-bg.png")
//            outputFile.inputStream().use { inputStream ->
//                inputStream.copyTo(outputStream)
//            }
//            println("Background removed image saved successfully.")
//        }
//    } ?: run {
//        println("Failed to create MediaStore entry.")
//    }
//}

//public fun alertDialog() {
//    val builder = AlertDialog.Builder(this@MainActivity)
//    builder.setTitle("Androidly Alert")
//    builder.setMessage("We have a message")
//
//    builder.setPositiveButton(android.R.string.ok) { dialog, which ->
//    }
//    builder.show()
//}





