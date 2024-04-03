package com.example.deto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.material.bottomnavigation.BottomNavigationView

class home2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home2)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    true
                }

                R.id.riwayat -> {
                    // Kode yang dijalankan ketika item "riwayat" dipilih
                    startActivity(Intent(this, riwayat::class.java))
                    true
                }

                R.id.chat -> {
                    // Kode yang dijalankan ketika item "chat" dipilih
                    true
                }

                R.id.profil -> {
                    // Kode yang dijalankan ketika item "profil" dipilih
                    true
                }

                else -> false
            }
        }
    }
}