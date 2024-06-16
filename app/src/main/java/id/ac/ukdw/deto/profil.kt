package id.ac.ukdw.deto

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import id.ac.ukdw.deto.databinding.ActivityProfilBinding
import id.ac.ukdw.deto.databinding.DialogEditHobiBinding
import id.ac.ukdw.deto.databinding.DialogEditNamaBinding
import id.ac.ukdw.deto.databinding.DialogEditTentangBinding
import id.ac.ukdw.deto.model.ImageItem
import id.ac.ukdw.deto.model.MyRecyclerViewAdapter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class profil : AppCompatActivity() {
    private lateinit var binding: ActivityProfilBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var textFullName: TextView
    private lateinit var textTentang: TextView
    private lateinit var textHobi: TextView
    private lateinit var btnLogout: Button

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private lateinit var filePath: Uri
    private lateinit var chooseImageLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textFullName = binding.namaprofil
        textTentang = binding.tentangUbah
        textHobi = binding.hobiUbah
        btnLogout = binding.btnlogout
        recyclerView = binding.recycleview

        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = MyRecyclerViewAdapter(mutableListOf())

        chooseImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    filePath = it
                    // Panggil fungsi yang sesuai berdasarkan konteks
                    if (isProfileImageUpload) {
                        uploadProfileImage()
                    } else {
                        uploadImageToGallery()
                    }
                }
            }

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
                if (success) {
                    // Panggil fungsi yang sesuai berdasarkan konteks
                    if (isProfileImageUpload) {
                        uploadProfileImage()
                    } else {
                        uploadImageToGallery()
                    }
                }
            }

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            firestore.collection("user").document(firebaseUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("fullName")
                        val umur = document.getString("age")
                        val tentang = document.getString("tentang")
                        val hobi = document.getString("hobi")
                        val profileImageUrl = document.getString("profileImage")
                        if (!name.isNullOrEmpty()) {
                            val fullNameWithAge = if (!umur.isNullOrEmpty()) {
                                "$name, $umur"
                            } else {
                                name
                            }
                            textFullName.text = fullNameWithAge
                        } else {
                            Toast.makeText(this, "Nama tidak tersedia", Toast.LENGTH_SHORT).show()
                        }
                        if (!tentang.isNullOrEmpty()) {
                            textTentang.text = tentang
                        }
                        if (!hobi.isNullOrEmpty()) {
                            textHobi.text = hobi
                        }
                        if (!profileImageUrl.isNullOrEmpty()) {
                            // Gunakan Glide untuk memuat gambar dari URL
                            Glide.with(this).load(profileImageUrl).into(binding.imageView5)
                        }
                    } else {
                        Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Gagal mengambil data: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.editNama.setOnClickListener {
            showEditNamaDialog()
        }

        binding.editTentang.setOnClickListener {
            showEditTentangDialog()
        }

        binding.editHobi.setOnClickListener {
            showEditHobiDialog()
        }

        btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.bottomNav.setOnItemSelectedListener { menuItem ->
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
                    startActivity(Intent(this, pesan::class.java))
                    true
                }

                R.id.profil -> {
                    true
                }

                else -> false
            }
        }

        binding.btnadd.setOnClickListener {
            // Tambahkan gambar ke galeri
            isProfileImageUpload = false
            showImageSourceDialog()
        }

        binding.imageView5.setOnClickListener {
            // Ganti gambar profil
            isProfileImageUpload = true
            chooseImageLauncher.launch("image/*")
        }

        loadImagesFromFirestore()
    }

    private var isProfileImageUpload: Boolean = false

    private fun showEditNamaDialog() {
        val dialogBinding = DialogEditNamaBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogBinding.root)

        with(builder) {
            setTitle("Edit Nama")
            setPositiveButton("Simpan") { dialog, _ ->
                val newName = dialogBinding.editTextNama.text.toString()
                if (newName.isNotEmpty()) {
                    updateNama(newName)
                } else {
                    Toast.makeText(this@profil, "Nama tidak boleh kosong", Toast.LENGTH_SHORT)
                        .show()
                }
                dialog.dismiss()
            }
            setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun updateNama(newName: String) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            firestore.collection("user").document(firebaseUser.uid)
                .update("fullName", newName)
                .addOnSuccessListener {
                    textFullName.text = newName
                    Toast.makeText(this, "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Gagal memperbarui nama: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun showEditTentangDialog() {
        val dialogBinding = DialogEditTentangBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogBinding.root)

        with(builder) {
            setTitle("Edit Tentang Saya")
            setPositiveButton("Simpan") { dialog, _ ->
                val newTentang = dialogBinding.editTextTentang.text.toString()
                if (newTentang.isNotEmpty()) {
                    updateTentang(newTentang)
                } else {
                    Toast.makeText(this@profil, "Tentang tidak boleh kosong", Toast.LENGTH_SHORT)
                        .show()
                }
                dialog.dismiss()
            }
            setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun updateTentang(newTentang: String) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            firestore.collection("user").document(firebaseUser.uid)
                .update("tentang", newTentang)
                .addOnSuccessListener {
                    textTentang.text = newTentang
                    Toast.makeText(this, "Tentang berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Gagal memperbarui tentang: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun showEditHobiDialog() {
        val dialogBinding = DialogEditHobiBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogBinding.root)

        with(builder) {
            setTitle("Edit Hobi")
            setPositiveButton("Simpan") { dialog, _ ->
                val newHobi = dialogBinding.editTextHobi.text.toString()
                if (newHobi.isNotEmpty()) {
                    updateHobi(newHobi)
                } else {
                    Toast.makeText(this@profil, "Hobi tidak boleh kosong", Toast.LENGTH_SHORT)
                        .show()
                }
                dialog.dismiss()
            }
            setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun updateHobi(newHobi: String) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            firestore.collection("user").document(firebaseUser.uid)
                .update("hobi", newHobi)
                .addOnSuccessListener {
                    textHobi.text = newHobi
                    Toast.makeText(this, "Hobi berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Gagal memperbarui hobi: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    // Create a unique file name for the image
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)

                    imageUri = FileProvider.getUriForFile(
                        this,
                        "${applicationContext.packageName}.fileprovider",
                        file
                    )

                    takePictureLauncher.launch(imageUri)
                }

                1 -> chooseImageLauncher.launch("image/*")
            }
        }
        builder.show()
    }

    private fun uploadProfileImage() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            val storageRef = storage.reference
            val profileImageRef = storageRef.child("profileImages/${firebaseUser.uid}.jpg")
            val uploadTask = profileImageRef.putFile(filePath)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    saveProfileImageUrlToFirestore(uri.toString())
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to upload image: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveProfileImageUrlToFirestore(imageUrl: String) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            firestore.collection("user").document(firebaseUser.uid)
                .update("profileImage", imageUrl)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()
                    // Gunakan Glide untuk memuat gambar dari URL yang baru diunggah
                    Glide.with(this).load(imageUrl).into(binding.imageView5)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Failed to update profile image: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun uploadImageToGallery() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            val storageRef = storage.reference
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val galleryImageRef = storageRef.child("galleryImages/${firebaseUser.uid}/$timeStamp.jpg")
            val uploadTask = galleryImageRef.putFile(filePath)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    saveGalleryImageUrlToFirestore(uri.toString())
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to upload image: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveGalleryImageUrlToFirestore(imageUrl: String) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            val userImagesRef = firestore.collection("userImages").document(firebaseUser.uid)
            userImagesRef.update("images", FieldValue.arrayUnion(imageUrl))
                .addOnSuccessListener {
                    Toast.makeText(this, "Image added to gallery", Toast.LENGTH_SHORT).show()
                    // Memperbarui RecyclerView dengan gambar yang baru ditambahkan
                    (recyclerView.adapter as MyRecyclerViewAdapter).addImage(ImageItem(imageUrl))
                }
                .addOnFailureListener { exception ->
                    // Jika dokumen tidak ada, buat dokumen baru dengan gambar pertama
                    val initialData = hashMapOf("images" to listOf(imageUrl))
                    userImagesRef.set(initialData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Image added to gallery", Toast.LENGTH_SHORT).show()
                            // Memperbarui RecyclerView dengan gambar yang baru ditambahkan
                            (recyclerView.adapter as MyRecyclerViewAdapter).addImage(ImageItem(imageUrl))
                        }
                        .addOnFailureListener { setException ->
                            Toast.makeText(
                                this,
                                "Failed to save image: ${setException.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
        }
    }

    private fun loadImagesFromFirestore() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            val userImagesRef = firestore.collection("userImages").document(firebaseUser.uid)
            userImagesRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val images = document.get("images") as List<String>?
                    images?.let {
                        (recyclerView.adapter as MyRecyclerViewAdapter).updateImages(it.map { url ->
                            ImageItem(url)
                        }.toMutableList())
                    }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Failed to load images: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
