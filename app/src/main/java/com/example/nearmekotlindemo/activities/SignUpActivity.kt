package com.example.nearmekotlindemo.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.constant.AppConstant
import com.example.nearmekotlindemo.databinding.ActivitySignUpBinding
import com.example.nearmekotlindemo.permissions.AppPermissions
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.utility.State
import com.example.nearmekotlindemo.viewModels.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.flow.collect

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var appPermissions: AppPermissions
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var username: String
    private lateinit var email: String
    private lateinit var password: String
    private var image: Uri? = null
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appPermissions = AppPermissions()
        loadingDialog = LoadingDialog(this)

        binding.btnBack.setOnClickListener { onBackPressed() }

        binding.txtLogin.setOnClickListener { onBackPressed() }

        binding.imgPick.setOnClickListener {
            if (appPermissions.isStorageOk(this))
                pickImage()
            else
                appPermissions.requestStoragePermission(this)
        }

        binding.btnSignUp.setOnClickListener {
            if (areFieldReady()) {
                if (image != null) {
                    lifecycleScope.launchWhenStarted {
                        loginViewModel.signUp(email, password, username, image!!).collect {
                            when (it) {
                                is State.Loading -> {
                                    if (it.flag == true)
                                        loadingDialog.startLoading()
                                }

                                is State.Success -> {
                                    loadingDialog.stopLoading()
                                    Snackbar.make(
                                        binding.root,
                                        it.data.toString(),
                                        Snackbar.LENGTH_SHORT
                                    ).show()

                                    onBackPressed()

                                }
                                is State.Failed -> {
                                    loadingDialog.stopLoading()
                                    Snackbar.make(
                                        binding.root,
                                        it.error,
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                } else {
                    Snackbar.make(
                        binding.root,
                        "Please select image",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AppConstant.STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage()
            } else {
                Snackbar.make(
                    binding.root,
                    "Storage Permission Denied",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                image = result.uri
                Glide.with(this).load(image).into(binding.imgPick)
            } else {
                Snackbar.make(
                    binding.root,
                    "Image not selected",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun pickImage() = CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(this)

    private fun areFieldReady(): Boolean {
        username = binding.edtUsername.text.trim().toString()
        email = binding.edtEmail.text.trim().toString()
        password = binding.edtPassword.text.trim().toString()

        var view: View? = null
        var flag = false

        when {
            username.isEmpty() -> {
                binding.edtUsername.error = "Field is required"
                view = binding.edtUsername
                flag = true
            }

            email.isEmpty() -> {
                binding.edtEmail.error = "Field is required"
                view = binding.edtEmail
                flag = true
            }
            password.isEmpty() -> {
                binding.edtPassword.error = "Field is required"
                view = binding.edtPassword
                flag = true
            }
            password.length < 8 -> {
                binding.edtPassword.error = "Minimum 8 characters"
                view = binding.edtPassword
                flag = true
            }
        }

        return if (flag) {
            view?.requestFocus()
            false
        } else
            true

    }
}