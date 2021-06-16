package com.example.nearmekotlindemo.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.databinding.ActivityForgetBinding
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.utility.State
import com.example.nearmekotlindemo.viewModels.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect

class ForgetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgetBinding
    private lateinit var loadingDialog: LoadingDialog
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)

        binding.btnBack.setOnClickListener { onBackPressed() }

        binding.btnForgetPassword.setOnClickListener {
            val email = binding.edtFEmail.text.trim().toString()

            if (email.isEmpty()) {
                binding.edtFEmail.error = "Field is required"
                binding.edtFEmail.requestFocus()
            } else {
                lifecycleScope.launchWhenStarted {
                    loginViewModel.forget(email).collect {
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
            }
        }
    }
}