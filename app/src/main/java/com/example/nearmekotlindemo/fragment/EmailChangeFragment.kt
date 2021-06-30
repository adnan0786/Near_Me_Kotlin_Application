package com.example.nearmekotlindemo.fragment

import android.content.Intent
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
import com.example.nearmekotlindemo.utility.LoadingDialog
import com.example.nearmekotlindemo.utility.State
import com.example.nearmekotlindemo.viewModels.LocationViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect


class EmailChangeFragment : Fragment() {

    private lateinit var binding: FragmentEmailChangeBinding
    private lateinit var loadingDialog: LoadingDialog
    private val locationViewModel: LocationViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEmailChangeBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar?.title = "New Email"
        loadingDialog = LoadingDialog(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUpdateEmail.setOnClickListener {
            val email = binding.edtUEmail.text.toString().trim()
            if (email.isEmpty()) {
                binding.edtUEmail.error = "Field is required"
                binding.edtUEmail.requestFocus()
            } else {
                lifecycleScope.launchWhenStarted {
                    locationViewModel.updateEmail(email).collect {
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


}