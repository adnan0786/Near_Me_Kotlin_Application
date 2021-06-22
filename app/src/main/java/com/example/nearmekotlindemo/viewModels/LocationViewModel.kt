package com.example.nearmekotlindemo.viewModels

import androidx.lifecycle.ViewModel
import com.example.nearmekotlindemo.repo.AppRepo

class LocationViewModel : ViewModel() {

    private val repo = AppRepo()

    fun getNearByPlace(url:String)=repo.getPlaces(url)
}