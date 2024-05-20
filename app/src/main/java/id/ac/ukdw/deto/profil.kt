package id.ac.ukdw.deto

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import id.ac.ukdw.deto.databinding.ActivityProfilBinding
import id.ac.ukdw.deto.databinding.DialogEditHobiBinding
import id.ac.ukdw.deto.databinding.DialogEditNamaBinding
import id.ac.ukdw.deto.databinding.DialogEditTentangBinding
import id.ac.ukdw.deto.model.ImageItem
import id.ac.ukdw.deto.model.MyRecyclerViewAdapter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textFullName = binding.namaprofil
        textTentang = binding.tentangUbah
        textHobi = binding.hobiUbah
        btnLogout = binding.btnlogout
        recyclerView = binding.recycleview

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = MyRecyclerViewAdapter(mutableListOf())

        chooseImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                filePath = it
                uploadImage()
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
                    } else {
                        Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal mengambil data: ${exception.message}", Toast.LENGTH_SHORT).show()
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
                    startActivity(Intent(this, riwayat::class.java))
                    true
                }

                R.id.profil -> {
                    true
                }

                else -> false
            }
        }

        binding.btnadd.setOnClickListener {
            chooseImage()
        }

        loadImagesFromFirestore()
    }

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
                    Toast.makeText(this@profil, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Gagal memperbarui nama: ${exception.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@profil, "Tentang tidak boleh kosong", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Tentang Saya berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal memperbarui Tentang Saya: ${exception.message}", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@profil, "Hobi tidak boleh kosong", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Gagal memperbarui hobi: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun chooseImage() {
        chooseImageLauncher.launch("image/*")
    }

    private fun uploadImage() {
        if (this::filePath.isInitialized) {
            val storageReference = storage.reference.child("images/" + UUID.randomUUID().toString())
            storageReference.putFile(filePath)
                .addOnSuccessListener {
                    Toast.makeText(this@profil, "Image Uploaded", Toast.LENGTH_SHORT).show()
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        saveImageUrlToDatabase(imageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@profil, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            firestore.collection("user").document(firebaseUser.uid)
                .update("imageUrl", imageUrl)
                .addOnSuccessListener {
                    Toast.makeText(this, "Image URL saved to database", Toast.LENGTH_SHORT).show()
                    addImageToRecyclerView(imageUrl)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to save image URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addImageToRecyclerView(imageUrl: String) {
        // Ambil adapter dari RecyclerView
        val adapter = recyclerView.adapter as? MyRecyclerViewAdapter

        // Buat objek ImageItem dengan imageUrl yang baru diunggah
        val newItem = ImageItem(imageUrl)

        // Tambahkan newItem ke RecyclerView
        adapter?.addItem(newItem)
    }

    private fun loadImagesFromFirestore() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            firestore.collection("user").document(firebaseUser.uid)
                .collection("images")
                .get()
                .addOnSuccessListener { result ->
                    val images = mutableListOf<ImageItem>()
                    for (document in result) {
                        val url = document.getString("imageUrl")
                        if (!url.isNullOrEmpty()) {
                            images.add(ImageItem(url))
                        }
                    }
                    val adapter = recyclerView.adapter as? MyRecyclerViewAdapter
                    adapter?.setItems(images)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to load images: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
