package id.ac.ukdw.deto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class jodoh : AppCompatActivity() {

    private lateinit var textPlayer1: TextView
    private lateinit var textPlayer2: TextView
    private lateinit var myPict: CircleImageView
    private lateinit var myPict2: CircleImageView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUserUid: String
    private var matchedUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jodoh)

        textPlayer1 = findViewById(R.id.player2)
        textPlayer2 = findViewById(R.id.player1)
        myPict = findViewById(R.id.myPict)
        myPict2 = findViewById(R.id.myPict2)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserUid = firebaseAuth.currentUser?.uid ?: ""

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            loadMatchData()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val btnNantiAja = findViewById<Button>(R.id.btnntar)
        btnNantiAja.setOnClickListener {
            // Kembali ke halaman home2
            val intent = Intent(this, home2::class.java)
            startActivity(intent)
            finish()
        }

        val btnPesan = findViewById<Button>(R.id.btnpesan)

        // Mendapatkan data pengguna yang cocok dari intent
        matchedUserId = intent.getStringExtra("matchedUserId")

        // Menambahkan listener klik pada tombol btnpesan
        btnPesan.setOnClickListener {
            // Memastikan ID pengguna yang cocok tidak kosong sebelum memulai aktivitas bubblechat
            matchedUserId?.let { userId ->
                // Menampilkan toast
                showToast("Navigating to chat...")

                // Menginisialisasi intent untuk memulai aktivitas bubblechat
                val intent = Intent(this, bubblechat::class.java)

                // Mengirim ID pengguna yang cocok sebagai parameter ke aktivitas bubblechat
                intent.putExtra("matchedUserId", userId)

                // Memulai aktivitas bubblechat
                startActivity(intent)
            }
        }

    }

    private fun loadMatchData() {
        // Retrieve the latest matched user document from the matches collection
        firestore.collection("matches").document(currentUserUid)
            .collection("matches")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    matchedUserId = document.getString("matchedUserId")

                    // Load the matched user's data
                    matchedUserId?.let { matchedId ->
                        firestore.collection("user").document(matchedId)
                            .get()
                            .addOnSuccessListener { matchedUserDoc ->
                                val matchedUserName = matchedUserDoc.getString("fullName")
                                val matchedUserImage = matchedUserDoc.getString("profileImage")

                                textPlayer2.text = matchedUserName
                                Picasso.get().load(matchedUserImage).into(myPict2)

                                // Load the current user's data
                                firestore.collection("user").document(currentUserUid)
                                    .get()
                                    .addOnSuccessListener { currentUserDoc ->
                                        val currentUserName = currentUserDoc.getString("fullName")
                                        val currentUserImage = currentUserDoc.getString("profileImage")

                                        textPlayer1.text = currentUserName
                                        Picasso.get().load(currentUserImage).into(myPict)
                                    }
                                    .addOnFailureListener { exception ->
                                        // Handle failure to load current user data
                                        showToast("Failed to load current user data: ${exception.message}")
                                    }
                            }
                            .addOnFailureListener { exception ->
                                // Handle failure to load matched user data
                                showToast("Failed to load matched user data: ${exception.message}")
                            }
                    }
                } else {
                    // If there are no matches
                    textPlayer1.text = "Belum ada match"
                    textPlayer2.text = "Belum ada match"
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure to load matches
                showToast("Failed to load matches: ${exception.message}")
            }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

}
