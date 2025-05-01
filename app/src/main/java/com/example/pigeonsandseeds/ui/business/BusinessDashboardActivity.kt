package com.example.pigeonsandseeds.ui.business

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.pigeonsandseeds.databinding.ActivityBusinessDashboardBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BusinessDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBusinessDashboardBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusinessDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupClickListeners()
        checkLocationPermission()
        loadBusinessData()
    }

    private fun setupClickListeners() {
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            finish()
        }

        binding.createOfferButton.setOnClickListener {
            startActivity(Intent(this, CreateOfferActivity::class.java))
        }

        binding.editProfileButton.setOnClickListener {
            startActivity(Intent(this, BusinessRegistrationActivity::class.java))
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            updateBusinessLocation()
        }
    }

    private fun updateBusinessLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val businessId = auth.currentUser?.uid ?: return@let
                    db.collection("businesses").document(businessId)
                        .update("location", com.google.firebase.firestore.GeoPoint(it.latitude, it.longitude))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to update location: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    private fun loadBusinessData() {
        val businessId = auth.currentUser?.uid ?: return
        db.collection("businesses").document(businessId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val businessName = document.getString("name") ?: ""
                    val currentOffer = document.getString("currentOffer") ?: "No active offer"
                    
                    binding.welcomeTextView.text = "Welcome, $businessName"
                    binding.currentOfferTextView.text = currentOffer
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load business data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateBusinessLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required for business location",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1002
    }
} 