package id.ac.ukdw.deto.firestore

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val gender: String = ""
)
