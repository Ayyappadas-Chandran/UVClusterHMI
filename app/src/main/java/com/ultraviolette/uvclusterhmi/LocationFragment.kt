package com.ultraviolette.uvclusterhmi

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import java.util.Locale

class LocationFragment : Fragment() {
    private lateinit var tvLongitudeValue: TextView
    private lateinit var tvLatitudeValue: TextView
    private lateinit var tvCityName: TextView
    private lateinit var btnRefresh: Button
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)

        btnRefresh.setOnClickListener {
            ensureLocationEnabled {
                fetchLocation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ensureLocationEnabled {
            fetchLocation()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun initView(view: View) {
        tvLongitudeValue = view.findViewById(R.id.tvLongitudeValue)
        tvLatitudeValue = view.findViewById(R.id.tvLatitudeValue)
        tvCityName = view.findViewById(R.id.tvCityName)
        btnRefresh = view.findViewById(R.id.btnRefresh)
    }

    private fun fetchLocation() {
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("LOCATION", "Permission not granted")
            return
        }
        stopLocationUpdates()
        locationListener = LocationListener { location ->
            updateUI(location)
        }
        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            0f,
            locationListener!!
        )

        Log.d("Location", "Location updates started")
    }

    private fun stopLocationUpdates() {
        locationListener?.let {
            locationManager?.removeUpdates(it)
        }
        locationListener = null
    }

    private fun updateUI(location: Location) {
        val lat = location.latitude
        val lon = location.longitude
        tvLatitudeValue.text = String.format("%.6f", lat)
        tvLongitudeValue.text = String.format("%.6f", lon)
        Log.d("Location", "Lat=$lat, Lon=$lon")
        getCityFromLocation(lat, lon)
    }

    private fun getCityFromLocation(lat: Double, lon: Double) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val city = addresses[0].locality ?: "Unknown"
                tvCityName.text = city
                Log.d("Location", "City=$city")
            }
        } catch (e: Exception) {
            Log.e("Location", "Geocoder failed: ${e.message}")
        }
    }

    private fun ensureLocationEnabled(onReady: () -> Unit) {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        Log.d("Location", "isGpsEnabled: $isGpsEnabled")

        if (!isGpsEnabled) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }
        onReady()
    }
}
