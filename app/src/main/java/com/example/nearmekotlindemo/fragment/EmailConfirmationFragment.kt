package com.example.nearmekotlindemo.fragment

import android.content.Intent
import android.os.Binder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.nearmekotlindemo.R
import com.example.nearmekotlindemo.activities.MainActivity
import com.example.nearmekotlindemo.databinding.FragmentEmailChangeBinding
import com.example.nearmekotlindemo.databinding.FragmentEmailConfirmationBinding
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.utility.State
import com.example.nearmekotlindemo.viewModels.LocationViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.collect


class EmailConfirmationFragment : Fragment() {

    private lateinit var binding: FragmentEmailConfirmationBinding
    private lateinit var emailFragmentArgs: EmailConfirmationFragmentArgs
    private var isPassword = false
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private val locationViewModel: LocationViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            FragmentEmailConfirmationBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity?)!!.supportActionBar?.title = "Email Confirmation"
        emailFragmentArgs = EmailConfirmationFragmentArgs.fromBundle(requireArguments())
        isPassword = emailFragmentArgs.isPassword
        firebaseAuth = Firebase.auth
        loadingDialog = LoadingDialog(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.edtCEmail.setText(firebaseAuth.currentUser?.email)

        binding.btnConfirmAccount.setOnClickListener {
            if (areFieldReady()) {
                val authCredential = EmailAuthProvider
                    .getCredential(email, password)

                lifecycleScope.launchWhenStarted {
                    locationViewModel.confirmEmail(authCredential).collect {
                        when (it) {
                            is State.Loading -> {
                                if (it.flag == true)
                                    loadingDialog.startLoading()
                            }

                            is State.Success -> {
                                loadingDialog.stopLoading()
                                if (isPassword) {
                                    Navigation.findNavController(view)
                                        .navigate(R.id.action_emailConfirmationFragment_to_passwordChangeFragment)

                                }else{
                                    Navigation.findNavController(view)
                                        .navigate(R.id.action_emailConfirmationFragment_to_emailChangeFragment)

                                }

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


    private fun areFieldReady(): Boolean {

        email = binding.edtCEmail.text?.trim().toString()
        password = binding.edtCPassword.text?.trim().toString()

        var view: View? = null
        var flag = false

        when {

            email.isEmpty() -> {
                binding.edtCEmail.error = "Field is required"
                view = binding.edtCEmail
                flag = true
            }
            password.isEmpty() -> {
                binding.edtCPassword.error = "Field is required"
                view = binding.edtCPassword
                flag = true
            }
            password.length < 8 -> {
                binding.edtCPassword.error = "Minimum 8 characters"
                view = binding.edtCPassword
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