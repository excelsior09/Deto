package id.ac.ukdw.deto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

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