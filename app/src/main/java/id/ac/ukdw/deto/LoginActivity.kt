package id.ac.ukdw.deto

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    lateinit var editEmail: EditText
    lateinit var editPassword: EditText
    lateinit var btnRegister: Button
    lateinit var btnLogin: Button

    var firebaseAuth = FirebaseAuth.getInstance()

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        editEmail = findViewById(R.id.email)
        editPassword = findViewById(R.id.password)
        btnRegister = findViewById(R.id.btn_register)
        btnLogin = findViewById(R.id.btn_login)

        btnLogin.setOnClickListener {
            if (editEmail.text.isNotEmpty() && editPassword.text.isNotEmpty()) {
                prosessLogin()
            } else {
                Toast.makeText(this, "Silahkan isi email dan password terlebih dahulu", LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun prosessLogin() {
        val email = editEmail.text.toString()
        val password = editPassword.text.toString()

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Logging")
            .setMessage("Silahkan tunggu ...")
            .setCancelable(false)
            .create()

        alertDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, error.localizedMessage, LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                alertDialog.dismiss()
            }
    }
}