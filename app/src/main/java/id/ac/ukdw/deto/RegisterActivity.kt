package id.ac.ukdw.deto

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import id.ac.ukdw.deto.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set up the spinner for gender selection
        ArrayAdapter.createFromResource(
            this,
            R.array.gender_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGender.adapter = adapter
        }

        binding.buttonLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.buttonRegister.setOnClickListener {
            val fullName = binding.textFullName.text.toString()
            val age = binding.textAgeLabel.text.toString()
            val email = binding.textEmail.text.toString()
            val location = binding.textLocation.text.toString()
            val password = binding.textPassword.text.toString()
            val confirmPassword = binding.textConfirmPassword.text.toString()
            val gender = binding.spinnerGender.selectedItem.toString()

            if (validateInput(fullName, age, email, location, password, confirmPassword)) {
                // Perform registration process
                registerUser(fullName, age, email, location, password, gender)
            }
        }
    }

    private fun validateInput(
        fullName: String,
        age: String,
        email: String,
        location: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (fullName.isEmpty() || age.isEmpty() || email.isEmpty() || location.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty()
        ) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        } else if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun registerUser(
        fullName: String,
        age: String,
        email: String,
        location: String,
        password: String,
        gender: String
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val userData = hashMapOf(
                        "fullName" to fullName,
                        "age" to age,
                        "email" to email,
                        "location" to location,
                        "gender" to gender
                    )
                    user?.let {
                        firestore.collection("user").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to store user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
