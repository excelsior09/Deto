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
import com.google.firebase.auth.userProfileChangeRequest

class RegisterActivity : AppCompatActivity() {
    lateinit var editFullName: EditText
    lateinit var editUmur: EditText
    lateinit var editEmail: EditText
    lateinit var editPassword: EditText
    lateinit var editPasswordConf: EditText
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
        setContentView(R.layout.activity_register)
        editFullName = findViewById(R.id.full_name)
        editUmur = findViewById(R.id.umur)
        editEmail = findViewById(R.id.email)
        editPassword = findViewById(R.id.password)
        editPasswordConf = findViewById(R.id.password_conf)
        btnRegister = findViewById(R.id.btn_register)
        btnLogin = findViewById(R.id.btn_login)

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        btnRegister.setOnClickListener {
            if (editFullName.text.isNotEmpty() && editEmail.text.isNotEmpty() && editPassword.text.isNotEmpty()) {
                if (editPassword.text.toString() == editPasswordConf.text.toString()) {
                    //Launch Register
                    processRegister()
                } else {
                    Toast.makeText(this, "Konfirmasi kata sandi harus sama", LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Silahkan isi dulu semua data", LENGTH_SHORT).show()
            }
        }
    }

    private fun processRegister() {
        val fullName = editFullName.text.toString()
        val umur = editUmur.text.toString()
        val email = editEmail.text.toString()
        val password = editPassword.text.toString()

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Logging")
            .setMessage("Silahkan tunggu ...")
            .setCancelable(false)
            .create()

        alertDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                alertDialog.dismiss()
                if (task.isSuccessful) {
                    val userUpdateProfile = userProfileChangeRequest {
                        displayName = fullName
                    }
                    val user = task.result?.user
                    user?.updateProfile(userUpdateProfile)
                        ?.addOnCompleteListener {
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                        ?.addOnFailureListener { error2 ->
                            Toast.makeText(this, error2.localizedMessage, LENGTH_SHORT).show()
                        }
                } else {
                    val error = task.exception?.localizedMessage ?: "Registrasi gagal"
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            }
    }
}