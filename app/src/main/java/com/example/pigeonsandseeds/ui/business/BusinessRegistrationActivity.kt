package com.example.pigeonsandseeds.ui.business

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pigeonsandseeds.databinding.ActivityBusinessRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class BusinessRegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBusinessRegistrationBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusinessRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        loadBusinessData()
    }

    private fun setupClickListeners() {
        binding.submitButton.setOnClickListener {
            saveBusinessData()
        }
    }

    private fun loadBusinessData() {
        val businessId = auth.currentUser?.uid ?: return
        db.collection("businesses").document(businessId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.nameInput.setText(document.getString("name") ?: "")
                    binding.addressInput.setText(document.getString("address") ?: "")
                    binding.phoneInput.setText(document.getString("phone") ?: "")
                    binding.categoryInput.setText(document.getString("category") ?: "")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load business data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveBusinessData() {
        val name = binding.nameInput.text.toString().trim()
        val address = binding.addressInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()
        val category = binding.categoryInput.text.toString().trim()

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val businessId = auth.currentUser?.uid ?: return
        val businessData = hashMapOf(
            "name" to name,
            "address" to address,
            "phone" to phone,
            "category" to category,
            "userId" to businessId
        )

        db.collection("businesses").document(businessId)
            .set(businessData)
            .addOnSuccessListener {
                Toast.makeText(this, "Business profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update business profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
} 