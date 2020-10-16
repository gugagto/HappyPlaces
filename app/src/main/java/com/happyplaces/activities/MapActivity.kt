package com.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.happyplaces.R
import com.happyplaces.models.HappyPlaceModel
import kotlinx.android.synthetic.main.activity_happy_place_details.*
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {


    private var hp:HappyPlaceModel?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if (intent.hasExtra("place"))
        {
            hp= intent.getSerializableExtra("place") as HappyPlaceModel
        }
        if (hp!=null)
        {
            setSupportActionBar(toolbar_map)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = hp!!.title
            toolbar_map.setNavigationOnClickListener {
                onBackPressed()
            }
            val supportMapFragment: SupportMapFragment =supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)

        }



    }

    override fun onMapReady(p0: GoogleMap?) {
        val position = LatLng(hp!!.latitude, hp!!.longitude)
        p0!!.addMarker(MarkerOptions().position(position).title(hp!!.location))            //bota o alfinete no mapa
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position,12f)               // zoom de 12x
        p0.animateCamera(newLatLngZoom)
    }


}
