package id.ac.ukdw.deto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth


class profil : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnadd: FloatingActionButton

    lateinit var textFullName: TextView
//    lateinit var btnLogout: Button

    val firebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)
        textFullName =findViewById(R.id.namaprofil)
//        btnLogout = findViewById<Button>(R.id.btnLogout)

        recyclerView = findViewById(R.id.recycleview)
        btnadd = findViewById(R.id.btnadd)

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser!=null){
            textFullName.text = firebaseUser.displayName
        }else{
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
//        btnLogout.setOnClickListener {
//            firebaseAuth.signOut()
//            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
//        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, home2::class.java))
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
                    true
                }

                else -> false
            }
        }
    }
}