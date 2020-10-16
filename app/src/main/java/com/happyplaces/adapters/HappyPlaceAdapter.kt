package com.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.happyplaces.R
import com.happyplaces.activities.AddHappyPlaceActivity
import com.happyplaces.activities.HappyPlaceDetailsActivity
import com.happyplaces.database.DatabaseHandler
import com.happyplaces.models.HappyPlaceModel
import kotlinx.android.synthetic.main.item_row.view.*
import kotlin.system.measureNanoTime

class HappyPlaceAdapter(val context: Context, var list:ArrayList<HappyPlaceModel>):RecyclerView.Adapter<HappyPlaceAdapter.ViewHolder> ()
  {

private var onClickListener:OnClickListener?=null

      class ViewHolder(view: View):RecyclerView.ViewHolder(view){
          val image=view.iv_place_image
          val tvTitle=view.tv_title
          val tvDesc=view.tv_description

      }


      interface OnClickListener{

        fun onClick(position: Int,model: HappyPlaceModel)

    }

    fun setOnclickListener(onClickListener: OnClickListener)
    {
        this.onClickListener=onClickListener
    }


      fun notifyEditItem(activity: Activity,position: Int,requestCode: Int)  //passa um adapter pq o adapter nao eh uma activity
      {
          val intent=Intent(context,AddHappyPlaceActivity::class.java)
          intent.putExtra("place",list[position])
          activity.startActivityForResult(intent, requestCode)  //startActivity for result pq tem um resquest code junto
          notifyItemChanged(position)

      }

      fun remove(position: Int)
      {
        val dbHandler= DatabaseHandler(context)
          dbHandler.remove(list[position])               //remove do bd
          list.removeAt(position)                         //remove da lista atual
          notifyItemRemoved(position)                       //reload the recycleview


      }


      override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_row,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model= list[position]
        holder.image.setImageURI(Uri.parse(model.image))
        holder.tvTitle.text=model.title
        holder.tvDesc.text=model.description
        holder.itemView.setOnClickListener {
            if (onClickListener!=null)
            {
                onClickListener!!.onClick(position,model)
            }
        }


    }

    override fun getItemCount(): Int {
      return  list.size
    }



}