package com.example.deto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnGetStarted: Button = findViewById(R.id.btnGetStartedButton)
        btnGetStarted.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when(v.id){
                R.id.btnGetStartedButton -> {
                    val pindahIntent = Intent(this, home2::class.java)
                    startActivity(pindahIntent)
                }
            }
        }
    }
}