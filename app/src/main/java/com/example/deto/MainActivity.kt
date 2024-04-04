package com.example.deto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStarted = findViewById<Button>(R.id.btnGetStartedButton)
        btnStarted.setOnClickListener {
            val intent = Intent(this, home2::class.java)
            startActivity(intent)
        }

    }
}