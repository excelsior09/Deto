package com.example.deto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class jodoh : AppCompatActivity() {

    lateinit var textFullName: TextView
    val firebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jodoh)
        textFullName =findViewById(R.id.player1)

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser!=null){
            textFullName.text = firebaseUser.displayName
        }else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        val btnNantiAja = findViewById<Button>(R.id.btnntar)
        btnNantiAja.setOnClickListener {
            val intent = Intent(this, home2::class.java)
            startActivity(intent)
        }

    }
}