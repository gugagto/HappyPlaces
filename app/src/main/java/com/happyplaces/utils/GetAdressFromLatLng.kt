package com.happyplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import java.lang.StringBuilder
import java.util.*

class GetAdressFromLatLng(context: Context,private val latitude:Double, private val longitude:Double):AsyncTask<Void,String,String>() {


    private val geocoder: Geocoder= Geocoder(context, Locale.getDefault())          //geocoder classe q transforma lat e long em lugares reais
    private lateinit var mAddressListener:AddressListener

    override fun doInBackground(vararg params: Void?): String {

        try {

            val adressList:List<Address>? = geocoder.getFromLocation(latitude,longitude,1)   //maxResults  retorna uma so lugar

            if( adressList!=null && adressList.isNotEmpty())
            {
                val address = adressList[0]
                //do in background return string e adress precisa ser um string
                val sb= StringBuilder()
                for (i in 0..address.maxAddressLineIndex)
                {
                    sb.append(address.getAddressLine(i)).append(" ")  // append adiciona espaco pra nao ficar junto
                }
                sb.deleteCharAt(sb.length-1)   // deleta o ultimo caracter no final
                return sb.toString()
            }

        }catch ( e :Exception)
        {
            e.printStackTrace()
        }

        return "not available yet"

    }


    override fun onPostExecute(result: String?) {

        if (result==null)
        {
            mAddressListener.onError()
        }
        else
        {
            mAddressListener.addressFound(result)
        }

        super.onPostExecute(result)
    }

    fun setAddressListener(addressListener: AddressListener){

        mAddressListener=addressListener


    }

    fun getAddress()
    {
       execute()                    //roda  a asncTask
    }


    interface AddressListener{

        fun  addressFound(adress:String?)
        fun  onError()

    }


}