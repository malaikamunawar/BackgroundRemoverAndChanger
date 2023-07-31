package com.example.removebackground

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.delay
import java.awt.font.TextAttribute

class splash : AppCompatActivity() {
    private lateinit var icon : ImageView
    private lateinit var text : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)
        text= findViewById(R.id.textView)
        icon = findViewById(R.id.iconimg)
        icon.animate().apply {
            duration= 1500
            rotationYBy(360f)
            rotationY(360f)
        }.start()
        text.animate().apply {
            duration= 1500
            floatArrayOf(360f)
            rotationY(360f)
        }.start()
        Handler().postDelayed({
            // on below line we are
            // creating a new intent
            val i = Intent(this, Mainmenu::class.java)
            startActivity(intent)
            // on below line we are
            // starting a new activity.
            startActivity(i)

            // on the below line we are finishing
            // our current activity.
            finish()
        }, 2500)



    }
}