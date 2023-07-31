package com.example.removebackground

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class bg : AppCompatActivity() {
    //    private var xDelta = 0
//    private var yDelta = 0
//    var initialImageWidth = 0
//    var initialImageHeight = 0
//    var initialTouchX = 0f
//    var initialTouchY = 0f
//    var initialScaleX = 1f
//    var initialScaleY = 1f
//    var initialTranslationX = 0f
//    var initialTranslationY = 0f
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialScaleX = 1f
    private var initialScaleY = 1f
    private var initialTranslationX = 0f
    private var initialTranslationY = 0f

    private var selectedImageUri: Uri? = null
    private lateinit var imageView: ImageView
    private lateinit var frameLayout: FrameLayout

    private var initialX = 0f
    private var initialY = 0f
    private var scaleFactor = 1.0f
    private var initialScale = 1f
    private var mode = Mode.NONE

    private enum class Mode {
        NONE, DRAG, ZOOM
    }

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private val PICK_IMAGE_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bg)


        frameLayout = findViewById(R.id.frameLayout)
        imageView = findViewById<ImageView>(R.id.bgimageview)

        val img1 = findViewById<ImageView>(R.id.img1)
        val img2 = findViewById<ImageView>(R.id.img2)
        val img3 = findViewById<ImageView>(R.id.img3)
        val img4 = findViewById<ImageView>(R.id.img4)
        val img5 = findViewById<ImageView>(R.id.img5)

        img1.setOnClickListener {
            openImagePicker()
        }

        img2.setOnClickListener {
            val bitmapDrawable = img2.drawable as? BitmapDrawable
            bitmapDrawable?.let { drawable ->
                frameLayout.background = drawable
            }
        }

        img3.setOnClickListener {
            val bitmapDrawable = img3.drawable as? BitmapDrawable
            bitmapDrawable?.let { drawable ->
                frameLayout.background = drawable
            }
        }

        img4.setOnClickListener {
            val bitmapDrawable = img4.drawable as? BitmapDrawable
            bitmapDrawable?.let { drawable ->
                frameLayout.background = drawable
            }
        }

        img5.setOnClickListener {
            val bitmapDrawable = img5.drawable as? BitmapDrawable
            bitmapDrawable?.let { drawable ->
                frameLayout.background = drawable
            }
        }

        val sv = findViewById<Button>(R.id.save)
        sv.setOnClickListener {
            captureFrameLayoutScreenshot()
        }
        val imagePath = intent.getStringExtra("imagePath")
        //val imagePath = "@drawable/nobg.jpg"
        println(imagePath)
        if (imagePath != null) {

            val bitmap = BitmapFactory.decodeFile(imagePath)
            imageView.setImageBitmap(bitmap)
        } else {
            Log.e("Error", "No image path received.")
        }

        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())

        imageView.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (mode == Mode.NONE) {
                        mode = Mode.DRAG
                        initialX = view.x - event.rawX
                        initialY = view.y - event.rawY
                    }
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    mode = Mode.ZOOM
                    initialScale = imageView.scaleX
                }

                MotionEvent.ACTION_MOVE -> {
                    if (mode == Mode.DRAG) {
                        view.animate().x(event.rawX + initialX).y(event.rawY + initialY)
                            .setDuration(0).start()
                    }
                }

                MotionEvent.ACTION_POINTER_UP -> {
                    mode = Mode.NONE
                }
            }

            // Pass the touch event to the scale gesture detector for pinch-to-zoom handling
            scaleGestureDetector.onTouchEvent(event)
            true
        }
    }


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f))
            imageView.scaleX = scaleFactor
            imageView.scaleY = scaleFactor
            return true

        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }
    private fun captureFrameLayoutScreenshot() {
        val frameLayout = findViewById<FrameLayout>(R.id.frameLayout)

        // Create a new Bitmap with the same dimensions as the FrameLayout
        val bitmap = Bitmap.createBitmap(frameLayout.width, frameLayout.height, Bitmap.Config.ARGB_8888)

        // Create a Canvas with the Bitmap and draw the FrameLayout onto it
        val canvas = android.graphics.Canvas(bitmap)
        frameLayout.draw(canvas)

        // Save the Bitmap as an image file
        saveBitmapToGallery(bitmap)
    }



    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val displayName = "FrameLayoutSnapshot" // Set the display name for the image
        val mimeType = "image/jpeg"

        // Insert the image into the MediaStore to save it to the gallery
        val contentResolver = applicationContext.contentResolver
        val imageUriString = MediaStore.Images.Media.insertImage(
            contentResolver, bitmap, displayName, null
        )

        // Convert the String path to a Uri
        val imageUri = Uri.parse(imageUriString)

        // Notify the gallery about the new image so it appears in the gallery app
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = imageUri
        applicationContext.sendBroadcast(mediaScanIntent)
        Toast.makeText(applicationContext, "Image saved to gallery.", Toast.LENGTH_SHORT).show()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Handle the selected image here (data?.data contains the image URI)
                val bitmap = getBitmapFromUri(uri)
                bitmap?.let {
                    frameLayout.background = BitmapDrawable(resources, bitmap)
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}


