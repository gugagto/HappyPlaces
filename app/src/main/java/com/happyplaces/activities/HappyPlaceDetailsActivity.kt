package com.happyplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.happyplaces.R
import com.happyplaces.models.HappyPlaceModel
import kotlinx.android.synthetic.main.activity_happy_place_details.*

class HappyPlaceDetailsActivity : AppCompatActivity() {



        var happyPlaceModel:HappyPlaceModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_details)

        setSupportActionBar(toolbar_happy_place_details)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_happy_place_details.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra("place"))
        {
            happyPlaceModel= intent.getSerializableExtra("place") as HappyPlaceModel
            supportActionBar?.title= happyPlaceModel!!.title
            image.setImageURI(Uri.parse(happyPlaceModel!!.image))
            tv_location.text=happyPlaceModel!!.location
            tv_description.text=happyPlaceModel!!.description

        }

        btn_map.setOnClickListener {
         val intent =Intent(this,MapActivity::class.java)
            intent.putExtra("place",happyPlaceModel)
            startActivity(intent)


        }





    }





}