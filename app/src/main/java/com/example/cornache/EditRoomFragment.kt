package com.example.cornache

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.cornache.data.LoginPreference
import com.example.cornache.data.ResultState
import com.example.cornache.data.dataStore
import com.example.cornache.databinding.ActivityEditRoomBinding
import com.example.cornache.databinding.FragmentEditRoomBinding
import com.example.cornache.viewmodel.EditRoomViewModel
import com.example.cornache.viewmodel.ViewModelFactory


class EditRoomFragment : Fragment() {
    private var _binding : FragmentEditRoomBinding?=null
    private val binding get()=_binding
    private lateinit var viewModel: EditRoomViewModel
    private lateinit var loginPreference: LoginPreference
    private lateinit var roomId:String
    private var currentImageUri: Uri?=null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginPreference = LoginPreference.getInstance(requireContext().dataStore)
        val factory : ViewModelFactory = ViewModelFactory.getInstance(requireContext(), loginPreference)
        viewModel = ViewModelProvider(this,factory)[EditRoomViewModel::class.java]
        val bundle = this.arguments
        roomId = bundle?.getString("roomId").toString()
        val name = bundle?.getString("name").toString()
        val description = bundle?.getString("description").toString()
        val image = bundle?.getString("image").toString()
        if (bundle!=null){
            binding?.apply {
                GlideApp.with(requireContext())
                    .load(image)
                    .into(imagePlaceholder)
                judulText.setText(name)
                DeskripsiText.setText(description)
            }
        }
        binding?.apply {
            imagePlaceholder.setOnClickListener { startGallery() }
            postRoom.setOnClickListener { updateRoom() }
            deleteRoom.setOnClickListener { deleteRoom() }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditRoomBinding.inflate(inflater,container,false)
        return binding?.root
    }

    private fun deleteRoom(){
        viewModel.deleteRoom(roomId).observe(requireActivity()){result ->
            if (result!=null){
                when(result){
                    is ResultState.Loading -> showLoading(true)
                    is ResultState.Success -> {
                        showLoading(false)
                        showToast(result.data.message.toString())
                        Intent(requireContext(),HomeActivity::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        }
                    }
                    is ResultState.Error -> {
                        showLoading(false)
                        showToast(result.error)
                    }
                }
            }
        }
    }
    private fun updateRoom(){
        currentImageUri?.let {
            val updatedName = binding?.judulText?.text.toString()
            val updatedDesc = binding?.DeskripsiText?.text.toString()
            val updatedImage = uriToFile(it,requireContext())
            viewModel.updateRoom(roomId,updatedName,updatedImage,updatedDesc).observe(requireActivity()){result ->
                if (result!=null){
                    when(result){
                        is ResultState.Loading -> showLoading(true)
                        is ResultState.Success -> {
                            showLoading(false)
                            Intent(requireContext(),HomeActivity::class.java).also {
                                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(it)
                            }
                        }
                        is ResultState.Error -> {
                            showLoading(false)
                            showToast(result.error)
                        }
                    }
                }
            }
        }
    }
    private fun startGallery(){
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    private fun showImage() {
        currentImageUri?.let {
            binding?.imagePlaceholder?.setImageURI(it)
        }
    }
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ){uri: Uri? ->
        if (uri != null){
            currentImageUri = uri
            showImage()
        }else{
            Toast.makeText(requireContext(), "Silahkan pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showLoading(isLoading:Boolean){
        binding?.progressBar3?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {

    }
}