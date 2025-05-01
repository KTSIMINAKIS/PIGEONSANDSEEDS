package com.example.pigeonsandseeds.ui.business

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pigeonsandseeds.databinding.ActivityCreateOfferBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.concurrent.TimeUnit

class CreateOfferActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateOfferBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateOfferBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.submitButton.setOnClickListener {
            createOffer()
        }
    }

    private fun createOffer() {
        val title = binding.titleInput.text.toString().trim()
        val description = binding.descriptionInput.text.toString().trim()
        val discount = binding.discountInput.text.toString().trim()
        val duration = binding.durationInput.text.toString().trim()

        if (title.isEmpty() || description.isEmpty() || discount.isEmpty() || duration.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val businessId = auth.currentUser?.uid ?: return
        val durationInHours = duration.toLongOrNull() ?: 24L
        val endTime = Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(durationInHours))

        val offer = hashMapOf(
            "title" to title,
            "description" to description,
            "discount" to discount,
            "startTime" to Date(),
            "endTime" to endTime,
            "businessId" to businessId,
            "isActive" to true
        )

        db.collection("offers")
            .add(offer)
            .addOnSuccessListener { documentReference ->
                // Update business document with current offer
                db.collection("businesses").document(businessId)
                    .update("currentOffer", title)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Offer created successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update business: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to create offer: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
} 