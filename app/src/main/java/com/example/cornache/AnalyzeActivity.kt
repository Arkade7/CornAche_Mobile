package com.example.cornache

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.cornache.CameraActivity.Companion.CAMERAX_RESULT
import com.example.cornache.data.LoginPreference
import com.example.cornache.data.ResultState
import com.example.cornache.data.dataStore
import com.example.cornache.databinding.ActivityAnalyzeBinding
import com.example.cornache.viewmodel.AnalyzeViewModel
import com.example.cornache.viewmodel.ViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AnalyzeActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAnalyzeBinding
    private var currentImageUri:Uri?=null
    private lateinit var loginPreference: LoginPreference
    private lateinit var viewModel:AnalyzeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAnalyzeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val preference = LoginPreference.getInstance(dataStore)
        val factory :ViewModelFactory = ViewModelFactory.getInstance(
            this,
            preference
        )
        viewModel = ViewModelProvider(this,factory)[AnalyzeViewModel::class.java]
        binding.btnGallery.setOnClickListener { startGallery() }
        binding.btnCamera.setOnClickListener { startCameraX() }
        binding.btnAnalyze.setOnClickListener { uploadImage() }
        setupNavigation()
    }
    private fun startGallery(){
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ){uri:Uri? ->
        if (uri != null){
            currentImageUri = uri
            showImage()
        }else{
            Toast.makeText(this, "Silahkan pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showImage() {
        currentImageUri?.let {
            binding.imagePlaceholder.setImageURI(it)
        }
    }
    private fun startCameraX(){
        val intent = Intent(this,CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }
    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if (it.resultCode == CAMERAX_RESULT){
            currentImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            showImage()
        }
    }
    private fun uploadImage(){
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri,this)
            Log.d("ImageFile", "showImage:${imageFile.path} ")
            val preference = LoginPreference.getInstance(dataStore)
            val userId = runBlocking { preference.getSession().first().userId }
            viewModel.analyzeImage(imageFile,userId).observe(this) {result ->
                if (result != null){
                    when(result){
                        is ResultState.Loading -> {
                            showLoading(true)
                        }
                        is ResultState.Success -> {
                            showLoading(false)
                            val resultPrediction = result.data.history?.prediction?.name
                            val resultImage = result.data.history?.prediction?.image
                            val bundle = Bundle().apply {
                                putString("nama",resultPrediction)
                                putString("gambar",resultImage)
                            }
                            Intent(this@AnalyzeActivity,ResultActivity::class.java).also {
                                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                it.putExtra(ResultActivity.DATA,bundle)
                                startActivity(it)
                            }
                        }
                        is ResultState.Error ->{
                            showLoading(false)
                            showToast(result.error)
                        }
                    }
                }
            }
        }
    }

    private fun setupNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_detect_disease
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.navigation_detect_disease -> {
                    startActivity(Intent(this, AnalyzeActivity::class.java))
                    true
                }
                R.id.navigation_edit_profile -> {
                    startActivity(Intent(this, EditProfileActivity::class.java))
                    true
                }
                R.id.navigation_logout -> {
                    logout()
                    true
                }
                R.id.navigation_chat -> {
                    startActivity(Intent(this, RoomActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            loginPreference.logout()
            startActivity(Intent(this@AnalyzeActivity, LoginActivity::class.java))
            finish()
        }
    }


    private fun showLoading(isLoading:Boolean){
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}