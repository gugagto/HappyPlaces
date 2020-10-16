package com.happyplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.happyplaces.R
import com.happyplaces.database.DatabaseHandler
import com.happyplaces.models.HappyPlaceModel
import com.happyplaces.utils.GetAdressFromLatLng
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_happy_place.*
import kotlinx.android.synthetic.main.activity_happy_place_details.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener , DatePickerDialog.OnDateSetListener{

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACES_REQUEST_CODE = 3
    }


    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null

    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private lateinit var mFusedLocationClient:FusedLocationProviderClient
    private var hp : HappyPlaceModel?=null



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        setSupportActionBar(toolbar_add_place)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_add_place.setNavigationOnClickListener {
            onBackPressed()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // https://www.tutorialkart.com/kotlin-android/android-datepicker-kotlin-example/

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)


            }

        if (intent.hasExtra("place"))
        {
            load()

        }

        if(!Places.isInitialized())
        {
            Places.initialize(this,resources.getString(R.string.maps_api_key))
        }


        et_date.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
        et_location.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        tv_current_location.setOnClickListener(this)
    }


   @SuppressLint("MissingPermission")                 //tira o erro que pede permissao pro user
   private fun newLocation()               //localizacao exato do gps
   {
       var mLocationRequest= LocationRequest()
       mLocationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY       // tipo de precisao
       mLocationRequest.interval=1000                                           // intervalo em seg
       mLocationRequest.numUpdates= 1

       mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper())
   }

    private val mLocationCallback= object : LocationCallback(){

        override fun onLocationResult(p0: LocationResult?) {

            val mLastLocation : Location = p0!!.lastLocation
            mLatitude=mLastLocation.latitude
            mLongitude=mLastLocation.longitude
            val addressTask = GetAdressFromLatLng(this@AddHappyPlaceActivity, mLatitude, mLongitude)
            addressTask.setAddressListener(object : GetAdressFromLatLng.AddressListener{

                override fun addressFound(adress: String?) {
                   et_location.setText(adress)
                }

                override fun onError() {
                    Log.e("get address:: ","ERROR")
                }


            })
            addressTask.getAddress()                        //executa toda a tarefa



        }
    }
    private fun load() {

        hp=intent.getSerializableExtra("place") as HappyPlaceModel
        if (hp!=null)
        {
            supportActionBar?.title="Edit Happy Place"
            et_title.setText(hp!!.title)
            et_date.setText(hp!!.date)
            et_location.setText(hp!!.location)
            et_description.setText(hp!!.description)

           saveImageToInternalStorage= Uri.parse(hp!!.image)       //salva a imagem na fun para poder salvar
            iv_place_image.setImageURI(saveImageToInternalStorage)
            mLatitude=hp!!.latitude
            mLongitude=hp!!.longitude
            btn_save.text="UPDATE"

        }

    }
        // funcao que verifica se tem permissao pra usar localizacao do usuario
    private fun isLocationEnabled():Boolean
    {
        val locationManager:LocationManager= getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onClick(v: View) {


        if (v.id == et_date.id) {
            val cal = Calendar.getInstance()
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            DatePickerDialog(this, this, year, month, day).show()
        }

        if (v.id == tv_add_image.id) {


            val pictureDialog = AlertDialog.Builder(this)    //cria um alert dialog padrao
            pictureDialog.setTitle("Select Action")
            val pictureDialogItems = arrayOf(
                "Select photo from gallery",
                "Capture photo from camera"
            )  // add 2 opcoes para escolha
            pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                when (which) {
                    0 -> choosePhotoFromGallery()
                    1 -> takePhotoFromCamera()
                }
            }
            pictureDialog.show()
        }

        if (v.id == btn_save.id) {

            if (checkNotEmpty()) {

                // precisa ver o id, se for 0 o bd atribui, senao vc precisa colocar o id correto ser encontrada p edicao
                val happyPlaceModel = HappyPlaceModel(
                    if (hp == null) 0 else hp!!.id,
                    et_title.text.toString(),
                    saveImageToInternalStorage.toString(),
                    et_description.text.toString(),
                    et_date.text.toString(),
                    et_location.text.toString(),
                    mLatitude,
                    mLongitude
                )
                val dbHandler = DatabaseHandler(this)

                if (hp == null) {
                    val addHappyPlace =
                        dbHandler.addPlace(happyPlaceModel)  //addPlace retorna um long

                    if (addHappyPlace > 0) {
                        Toast.makeText(this, "Place inserted successfully.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    dbHandler.editPlace(happyPlaceModel)
                    Toast.makeText(this, "Place updated successfully.", Toast.LENGTH_SHORT).show()
                }


                finish()
            }
        }

        if (v.id == et_location.id) {
            try {
                val fields = listOf(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
                )
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this)
                startActivityForResult(intent, PLACES_REQUEST_CODE)


            } catch (e: Exception) {
                e.printStackTrace()

            }
        }

        if (v.id == tv_current_location.id) {

            if (!isLocationEnabled())
            {

                Toast.makeText(this,"Please turn on location permission",Toast.LENGTH_SHORT).show()


            }else{

                Dexter.withActivity(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION).withListener(object :MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                        if (report!!.areAllPermissionsGranted())
                        {
                           newLocation()
                        }


                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }


                }).onSameThread().check()


            }
        }
    }

private fun checkNotEmpty():Boolean
{

    if (et_date.text.isNullOrEmpty())
    {
        val cal=Calendar.getInstance()
        val sdf=SimpleDateFormat("dd/MM/yyyy")
        val format = sdf.format(cal.time)
        et_date.setText(format.toString())
    }

    if (et_title.text.isNullOrEmpty() ||  et_description.text.isNullOrEmpty()  || saveImageToInternalStorage==null )
    {
        Toast.makeText(this,"Empty field",Toast.LENGTH_SHORT).show()
        return false
    }

     return true
}



    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

        val cal = Calendar.getInstance()
        cal.set(year, month, dayOfMonth)
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val format = sdf.format(cal.time)
        et_date.setText(format)

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        // Here this is used to get an bitmap from URI
                        @Suppress("DEPRECATION")
                        val selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                        saveImageToInternalStorage =
                            saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")

                        iv_place_image!!.setImageBitmap(selectedImageBitmap) // Set the selected image from GALLERY to imageView.
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity, "Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (requestCode == CAMERA) {

                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap // Bitmap from camera

                 saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                Log.e("Saved Image : ", "Path :: $saveImageToInternalStorage")

                iv_place_image!!.setImageBitmap(thumbnail) // Set to the imageView.
            }

            else if (requestCode== PLACES_REQUEST_CODE)
            {
                val place:Place = Autocomplete.getPlaceFromIntent(data!!)
                et_location.setText(place.address)
                mLatitude=place.latLng!!.latitude
                mLongitude= place.latLng!!.longitude

            }


        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cancelled", "Cancelled")
        }
    }




    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                    // Here after all the permission are granted launch the gallery to select and image.
                    if (report!!.areAllPermissionsGranted()) {

                        val galleryIntent = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )

                        startActivityForResult(galleryIntent, GALLERY)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread()
            .check()
    }


    private fun takePhotoFromCamera() {

        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    // Here after all the permission are granted launch the CAMERA to capture an image.
                    if (report!!.areAllPermissionsGranted()) {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread()
            .check()
    }

    /**
     * A function used to show the alert dialog when the permissions are denied and need to allow it from settings app info.
     */
    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature.")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }


    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {       //uri retorna o local de armazenamento


        val wrapper = ContextWrapper(applicationContext)               //para poder acessar o diretorio de armazenamento

        // Initializing a new file
        // The bellow line return a directory in internal storage
        /**
         * The Mode Private here is
         * File creation mode: the default mode, where the created file can only
         * be accessed by the calling application (or all applications sharing the
         * same user ID).
         */
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)

        // Create a file to save the image
        file = File(file, "${UUID.randomUUID()}.jpg")     //cria o arquivo com um nome unico de usuario

        try {

            val stream: OutputStream = FileOutputStream(file)          //passa o arquivo pra ser salvo
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()

        } catch (e: IOException) { // Catch the exception
            e.printStackTrace()
        }


        return Uri.parse(file.absolutePath)             //retorna o caminho onde esta salvo o arquivo
    }


}