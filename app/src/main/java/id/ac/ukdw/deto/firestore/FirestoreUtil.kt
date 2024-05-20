package id.ac.ukdw.deto.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val currentUserDocRef get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().uid
        ?: throw NullPointerException("UID is null.")}")

    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(
                    uid = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    displayName = FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                    email = FirebaseAuth.getInstance().currentUser?.email ?: ""
                )
                currentUserDocRef.set(newUser).addOnCompleteListener {
                    onComplete()
                }
            } else {
                onComplete()
            }
        }
    }

    fun updateCurrentUser(name: String = "", email: String = "", gender: String = "") {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["displayName"] = name
        if (email.isNotBlank()) userFieldMap["email"] = email
        if (gender.isNotBlank()) userFieldMap["gender"] = gender
        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener {
            onComplete(it.toObject(User::class.java)!!)
        }
    }
}