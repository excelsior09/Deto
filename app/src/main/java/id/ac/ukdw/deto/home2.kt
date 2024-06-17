package id.ac.ukdw.deto

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.squareup.picasso.Picasso
import java.util.Date

class home2 : AppCompatActivity() {

    private lateinit var btnFollow: ImageButton
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var imageView: ImageView
    private lateinit var namaTextView: TextView
    private lateinit var lokasiTextView: TextView
    private lateinit var isiHobiTextView: TextView

    private var usersList: List<QueryDocumentSnapshot> = listOf()
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home2)

        imageView = findViewById(R.id.fotoStrangers)
        namaTextView = findViewById(R.id.namaStrangers)
        lokasiTextView = findViewById(R.id.lokasiStrangers)
        isiHobiTextView = findViewById(R.id.isiHobiStrangers)
        btnFollow = findViewById(R.id.btnFollow)

        val btnPrev = findViewById<ImageButton>(R.id.btnPrev)
        val btnNext = findViewById<ImageButton>(R.id.btnNext)

        btnPrev.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                displayUserData(usersList[currentIndex])
            }
        }

        btnNext.setOnClickListener {
            if (currentIndex < usersList.size - 1) {
                currentIndex++
                displayUserData(usersList[currentIndex])
            }
        }

        loadStrangerData()

        btnFollow.setOnClickListener {
            val matchedUserId = usersList[currentIndex].id
            checkAndMatchUsers(currentUserUid!!, matchedUserId)
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
                    startActivity(Intent(this, pesan::class.java))
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

    private fun loadStrangerData() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            firestore.collection("user")
                .document(currentUserUid!!)
                .get()
                .addOnSuccessListener { document ->
                    val gender = document.getString("gender") ?: ""
                    val oppositeGender = if (gender == "Laki-Laki") "Perempuan" else "Laki-Laki"
                    loadStrangerData(oppositeGender)
                }
                .addOnFailureListener { exception ->
                    // Handle any errors here
                }
        }
    }

    private fun loadStrangerData(gender: String) {
        getCurrentUserGender { currentGender ->
            firestore.collection("user")
                .whereEqualTo("gender", gender)
                .get()
                .addOnSuccessListener { documents ->
                    usersList = documents.documents.filter {
                        it.getString("gender") != currentGender
                    } as List<QueryDocumentSnapshot>
                    if (usersList.isNotEmpty()) {
                        displayUserData(usersList[currentIndex])
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors here
                }
        }
    }

    private fun displayUserData(userDocument: QueryDocumentSnapshot) {
        val name = userDocument.getString("fullName")
        val umur = userDocument.getString("age")
        val location = userDocument.getString("location")
        val hobby = userDocument.getString("hobi")
        val profileImageUrl = userDocument.getString("profileImage")

        val fullNameWithAge = if (!umur.isNullOrEmpty()) {
            "$name, $umur"
        } else {
            name
        }

        namaTextView.text = fullNameWithAge
        lokasiTextView.text = location
        isiHobiTextView.text = hobby

        // Load profile image using Picasso
        Picasso.get().load(profileImageUrl).into(imageView)

    }

    private fun getCurrentUserGender(callback: (String) -> Unit) {
        val userDoc = firestore.collection("user").document(currentUserUid!!)
        userDoc.get().addOnSuccessListener { document ->
            if (document != null) {
                val gender = document.getString("gender") ?: ""
                callback(gender)
            } else {
                callback("")
            }
        }
            .addOnFailureListener {
                callback("")
            }
    }

    private fun checkAndMatchUsers(currentUserUid: String, matchedUserId: String) {
        if (currentUserUid.isEmpty() || matchedUserId.isEmpty()) return

        val currentMatchRef = firestore.collection("matches").document(currentUserUid)
            .collection("matches").document(matchedUserId)
        val reverseMatchRef = firestore.collection("matches").document(matchedUserId)
            .collection("matches").document(currentUserUid)

        firestore.runTransaction { transaction ->
            val currentMatchSnapshot = transaction.get(currentMatchRef)
            val reverseMatchSnapshot = transaction.get(reverseMatchRef)

            val isCurrentMatched = currentMatchSnapshot.exists()
            val isReverseMatched = reverseMatchSnapshot.exists()

            val timestamp = com.google.firebase.Timestamp(Date())

            if (isReverseMatched) {
                // If reverse match exists, both users have mutually matched
                transaction.set(currentMatchRef, mapOf(
                    "matchedUserId" to matchedUserId,
                    "timestamp" to timestamp
                ))
                transaction.set(reverseMatchRef, mapOf(
                    "matchedUserId" to currentUserUid,
                    "timestamp" to timestamp
                ))
                runOnUiThread {
                    Toast.makeText(this, "It's a match!", Toast.LENGTH_SHORT).show()
                }
                // Navigate to jodoh::class.java
                startActivity(Intent(this, jodoh::class.java).apply {
                    putExtra("matchedUserId", matchedUserId)
                })
            } else {
                // Only set the match for the current user
                transaction.set(currentMatchRef, mapOf(
                    "matchedUserId" to matchedUserId,
                    "timestamp" to timestamp
                ))
                runOnUiThread {
                    Toast.makeText(this, "Followed!", Toast.LENGTH_SHORT).show()
                }
            }

            // Update following and followers collections
            val followingRef = firestore.collection("following").document(currentUserUid)
                .collection("user").document(matchedUserId)
            val followerRef = firestore.collection("followers").document(matchedUserId)
                .collection("users").document(currentUserUid)

            transaction.set(followingRef, mapOf("followedUserId" to matchedUserId))
            transaction.set(followerRef, mapOf("followerUserId" to currentUserUid))
        }
            .addOnFailureListener { exception ->
                // Handle transaction failure
                runOnUiThread {
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
