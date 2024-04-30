package id.ac.ukdw.deto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView

class riwayat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, home2::class.java))
                    true
                }

                R.id.riwayat -> {
                    true
                }

                R.id.chat -> {
                    startActivity(Intent(this, riwayat::class.java))
                    true
                }

                R.id.profil -> {
                    startActivity(Intent(this, profil::class.java))
                    true
                }

                else -> false
            }
        }
    }
}