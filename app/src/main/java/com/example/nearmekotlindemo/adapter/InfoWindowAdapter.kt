package com.example.nearmekotlindemo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import com.example.nearmekotlindemo.databinding.InfoWindowLayoutBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.SphericalUtil
import kotlin.math.roundToInt

class InfoWindowAdapter(private val location: Location, context: Context) :
    GoogleMap.InfoWindowAdapter {
    private val binding: InfoWindowLayoutBinding = InfoWindowLayoutBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    @SuppressLint("SetTextI18n")
    override fun getInfoWindow(marker: Marker): View {
        binding.txtLocationName.text = marker.title
        val distance = SphericalUtil.computeDistanceBetween(
            LatLng(
                location.latitude, location.longitude
            ), marker.position
        )

        if (distance.roundToInt() > 1000) {
            val kilometers = (distance / 1000).roundToInt()
            binding.txtLocationDistance.text = "$kilometers KM"
        } else {
            binding.txtLocationDistance.text = "${distance.roundToInt()} Meters"

        }
        val speed = location.speed
        if (speed.roundToInt() > 0) {
            val time = distance / speed
            binding.txtLocationTime.text = "${time.roundToInt()} sec"
        } else
            binding.txtLocationTime.text = "N/A"

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun getInfoContents(marker: Marker): View {
        binding.txtLocationName.text = marker.title
        val distance = SphericalUtil.computeDistanceBetween(
            LatLng(
                location.latitude, location.longitude
            ), marker.position
        )

        if (distance.roundToInt() > 1000) {
            val kilometers = (distance / 1000).roundToInt()
            binding.txtLocationDistance.text = "$kilometers KM"
        } else {
            binding.txtLocationDistance.text = "${distance.roundToInt()} Meters"

        }
        val speed = location.speed
        if (speed.roundToInt() > 0) {
            val time = distance / speed
            binding.txtLocationTime.text = "${time.roundToInt()} sec"
        } else
            binding.txtLocationTime.text = "N/A"

        return binding.root
    }
}