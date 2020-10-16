package com.happyplaces.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.happyplaces.R
import com.happyplaces.adapters.HappyPlaceAdapter
import com.happyplaces.database.DatabaseHandler
import com.happyplaces.models.HappyPlaceModel
import kotlinx.android.synthetic.main.activity_main.*
import pl.kitek.rvswipetodelete.SwipeToDeleteCallback
import pl.kitek.rvswipetodelete.SwipeToEditCallback

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, AddHappyPlaceActivity::class.java)
            startActivity(intent)
        }

        getAllPlaces()
        swipeHandler()




    }




    private fun swipeHandler() {

        val swiper = object :SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter=rv_list.adapter  as HappyPlaceAdapter
              adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition,1)
            }

        }
        val editItemTouchHelper =ItemTouchHelper(swiper)
        editItemTouchHelper.attachToRecyclerView(rv_list)


        val deleteSwiper = object :SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter=rv_list.adapter  as HappyPlaceAdapter
                adapter.remove(viewHolder.adapterPosition)
            }

        }
        val deleteItemTouchHelper =ItemTouchHelper(deleteSwiper)
       deleteItemTouchHelper.attachToRecyclerView(rv_list)





    }

    override fun onResume() {
        super.onResume()
      getAllPlaces()


    }


    private fun getAllPlaces() {

        val dbHandler = DatabaseHandler(this)
        val getHappyPlacesList = dbHandler.getAll()
        rv_list.setHasFixedSize(true)


        if (getHappyPlacesList.size > 0) {

           rv_list.layoutManager=LinearLayoutManager(this)
            val happyPlaceAdapter = HappyPlaceAdapter(this, getHappyPlacesList)
            rv_list.adapter= happyPlaceAdapter
            rv_list.setHasFixedSize(true)
          happyPlaceAdapter.setOnclickListener(object :HappyPlaceAdapter.OnClickListener{
              override fun onClick(position: Int, model: HappyPlaceModel) {
                  val intent=Intent(this@MainActivity,HappyPlaceDetailsActivity::class.java)
                    intent.putExtra("place",model)
                  startActivity(intent)

              }

          })
        }
    }

}