package com.example.nearmekotlindemo.fragment

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
import com.example.nearmekotlindemo.databinding.FragmentPasswordChangeBinding
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.utility.State
import com.example.nearmekotlindemo.viewModels.LocationViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect


class PasswordChangeFragment : Fragment() {

    private lateinit var binding: FragmentPasswordChangeBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var password: String
    private val locationViewModel: LocationViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPasswordChangeBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar?.title = "New Password"
        loadingDialog = LoadingDialog(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUpdatePassword.setOnClickListener {
            if (areFieldReady()) {
                lifecycleScope.launchWhenStarted {
                    locationViewModel.updatePassword(password).collect {
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

                                Navigation.findNavController(requireView())
                                    .popBackStack()

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


        password = binding.edtUPassword.text?.trim().toString()

        var view: View? = null
        var flag = false

        when {

            password.isEmpty() -> {
                binding.edtUPassword.error = "Field is required"
                view = binding.edtUPassword
                flag = true
            }
            password.length < 8 -> {
                binding.edtUPassword.error = "Minimum 8 characters"
                view = binding.edtUPassword
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