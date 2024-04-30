package id.ac.ukdw.deto

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import id.ac.ukdw.deto.R

class home2 : AppCompatActivity(){
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home2)

        val followButton = findViewById<ImageButton>(R.id.follow)
        followButton.setOnClickListener {
            startActivity(Intent(this, jodoh::class.java))
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    true
                }

                R.id.riwayat -> {
                    startActivity(Intent(this, riwayat::class.java))
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