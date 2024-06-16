package id.ac.ukdw.deto

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class bubblechat : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var messageContainer: LinearLayout
    private lateinit var etMessage: EditText

    private var matchedUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bubblechat)

        messageContainer = findViewById(R.id.messageContainer)
        etMessage = findViewById(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)

        // Retrieve matched user ID from intent
        matchedUserId = intent.getStringExtra("matchedUserId")

        if (matchedUserId != null && currentUserUid != null) {
            loadMatchedUserProfile(matchedUserId!!)
            getChatHistory(matchedUserId!!)

            btnSend.setOnClickListener {
                val messageText = etMessage.text.toString().trim()
                if (!TextUtils.isEmpty(messageText)) {
                    saveMessageToChatHistory(currentUserUid!!, matchedUserId!!, messageText)
                    etMessage.text.clear() // Clear the input after sending the message
                }
            }
        } else {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Set up back button functionality
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun saveMessageToChatHistory(senderId: String, receiverId: String, message: String) {
        val chatMessageRef = firestore.collection("chat_history").document(senderId)
            .collection("chat_messages").document()

        val timestamp = System.currentTimeMillis()

        val messageData = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "message" to message,
            "timestamp" to timestamp
        )

        chatMessageRef.set(messageData)
            .addOnSuccessListener {
                showMessage(senderId, message, timestamp)
                saveMessageToReceiverChatHistory(receiverId, senderId, message, timestamp)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveMessageToReceiverChatHistory(receiverId: String, senderId: String, message: String, timestamp: Long) {
        val receiverChatRef = firestore.collection("chat_history").document(receiverId)
            .collection("chat_messages").document()

        val messageData = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "message" to message,
            "timestamp" to timestamp
        )

        receiverChatRef.set(messageData)
    }

    private fun getChatHistory(matchedUserId: String) {
        val chatHistoryRef = firestore.collection("chat_history").document(currentUserUid!!)
            .collection("chat_messages")

        chatHistoryRef.orderBy("timestamp", Query.Direction.ASCENDING).get()
            .addOnSuccessListener { documents ->
                messageContainer.removeAllViews()

                for (document in documents) {
                    val senderId = document.getString("senderId")
                    val message = document.getString("message")
                    val timestamp = document.getLong("timestamp") ?: 0L

                    showMessage(senderId, message, timestamp)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showMessage(senderId: String?, message: String?, timestamp: Long) {
        if (!senderId.isNullOrEmpty() && !message.isNullOrEmpty()) {
            val messageLayout = layoutInflater.inflate(
                if (senderId == currentUserUid) R.layout.item_sent_message else R.layout.item_received_message,
                messageContainer,
                false
            )

            val tvMessage = messageLayout.findViewById<TextView>(R.id.tvMessage)
            val tvTime = messageLayout.findViewById<TextView>(R.id.tvTime)

            tvMessage.text = message
            tvTime.text = android.text.format.DateFormat.format("hh:mm a", timestamp)

            messageContainer.addView(messageLayout)
        }
    }

    @SuppressLint("WrongViewCast")
    private fun loadMatchedUserProfile(matchedUserId: String) {
        val profileImageView = findViewById<ImageView>(R.id.profileImageStranger)
        val profileNameView = findViewById<TextView>(R.id.profileNameStranger)

        firestore.collection("user").document(matchedUserId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val fullName = document.getString("fullName")
                    val profileImageUrl = document.getString("profileImage")

                    profileNameView.text = fullName
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Picasso.get().load(profileImageUrl).into(profileImageView)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
