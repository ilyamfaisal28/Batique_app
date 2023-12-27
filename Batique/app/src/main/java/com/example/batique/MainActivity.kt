package com.example.batique

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.LeadingMarginSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.example.batique.databinding.ActivityMainBinding
import com.example.batique.ml.ModelMobilenetv2
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import kotlin.math.min


class MainActivity : AppCompatActivity() {

//    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var textOutput0: TextView
    private lateinit var textOutput1: TextView
    private lateinit var textOutput2: TextView
    private lateinit var textScore0: TextView
    private lateinit var textScore1: TextView
    private lateinit var textScore2: TextView

    private val galleryRequestCode = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme from preference
        applyThemeFromPreference()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setIcon(R.drawable.logo_svg)

        imageView = binding.imageView
        button = binding.btnCaptureImage
        textOutput0 = binding.textOutput0
        textOutput1 = binding.textOutput1
        textOutput2 = binding.textOutput2
        textScore0 = binding.textScore0
        textScore1 = binding.textScore1
        textScore2 = binding.textScore2

        val buttonLoad = binding.btnLoadImage

        button.setOnClickListener{
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePicturePreview.launch(null)
            }else{
                requestPermission.launch(android.Manifest.permission.CAMERA)
            }
        }
        buttonLoad.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
//                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//                intent.type = "image/*"
                val intent = Intent(Intent.ACTION_PICK)
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                onresult.launch(intent)
            }else{
                requestPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        // to redirect user to google search for the scientific name
        textOutput0.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=Batik ${textOutput0.text}"))
            startActivity(intent)
        }
        textOutput1.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=Batik ${textOutput1.text}"))
            startActivity(intent)
        }
        textOutput2.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=Batik ${textOutput2.text}"))
            startActivity(intent)
        }

        // to download image when long press on ImageView
        imageView.setOnLongClickListener{
            requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return@setOnLongClickListener true
        }
        textInstructions()
//        binding.information0.text = Html.fromHtml(R.string.steps_of_how_to_use.toString(), Html.FROM_HTML_MODE_COMPACT)
    }

    //Crop image to square ratio
    private fun getSquareCropDimensionForBitmap(bitmap: Bitmap): Int {
        //use the smallest dimension of the image to crop to
        return min(bitmap.width, bitmap.height)
    }

    //request camera permission
    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){granted->
        if (granted){
            takePicturePreview.launch(null)
        }else{
            Toast.makeText(this, "Permission Denied! Check again", Toast.LENGTH_SHORT).show()
        }
    }

    // launch camera and take picture
    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){bitmap->
        if (bitmap != null){
            val dimension: Int = getSquareCropDimensionForBitmap(bitmap)
            val croppedBitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)
            imageView.setImageBitmap(croppedBitmap)
            outputGenerator(bitmap)
            showViews()
        }
    }

    //to get image from gallery
    private val onresult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        Log.i("TAG", "This is the result: ${result.data} ${result.resultCode}")
        onResultReceived(galleryRequestCode, result)
    }

    private fun onResultReceived(requestCode: Int, result: ActivityResult?){
        when(requestCode){
            galleryRequestCode ->{
                if (result?.resultCode == Activity.RESULT_OK){
                    result.data?.data?.let{uri ->
                        Log.i("TAG", "onResultReceived: $uri")
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                        val dimension: Int = getSquareCropDimensionForBitmap(bitmap)
                        val croppedBitmap = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)
                        imageView.setImageBitmap(croppedBitmap)
//                        imageView.setImageBitmap(bitmap)
                        outputGenerator(bitmap)
                        showViews()
                    }
                }else{
                    Log.e("TAG", "onActivityResult: error in selecting image")
                }
            }
        }
    }

    private fun outputGenerator(bitmap: Bitmap){
        val imageSize = 224
        val imageProcessor = ImageProcessor.Builder()
//            .add(ResizeWithCropOrPadOp(imageSize, imageSize))
            .add(ResizeOp(imageSize, imageSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0.0f, 255.0f))
            .build()
//        var resized_img = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        //declaring tensorflow lite model variable
        val model = ModelMobilenetv2.newInstance(this)

        // converting bitmap into tensorflow image
//        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Creates inputs for reference.
//        val image = TensorImage.fromBitmap(newBitmap)
        var tImage = TensorImage(DataType.FLOAT32)
        tImage.load(bitmap)
        tImage = imageProcessor.process(tImage)
        val ss = tImage.tensorBuffer.floatArray.joinToString(", ", "[", "]")
        Log.i("taged", "tensorImage: $ss")
        val byteBuffer = tImage.buffer
        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, imageSize, imageSize, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val confidences = outputFeature0.floatArray
//        var listIndex: MutableList<Int>  = mutableListOf()
        var maxPosition = 0
        var maxConfidence = 0F
        var second = 0F
        var secondIdx = 0
        var third = 0F
        var thirdIdx = 0
        for ((i, value) in confidences.withIndex()) {
            if (value > maxConfidence) {
                maxConfidence = value
                maxPosition = i
            }
        }

        for ((i, value) in confidences.withIndex()) {
            if (value < maxConfidence && value > second) {
                second = value
                secondIdx = i
                Log.i("taged", "Kedua Terbesar: $second")
            }
        }
        for ((i, value) in confidences.withIndex()) {
            if (value < maxConfidence && value < second && value > third){
                third = value
                thirdIdx = i
                Log.i("taged", "Ketiga Terbesar: $third")
            }
        }

        val classes = arrayOf("Buketan", "Jlamprang", "Liong", "Megamendung", "Negatif", "Singa Barong", "Tujuh Rupa")
        textOutput0.text = classes[maxPosition]
        textScore0.text = String.format(getString(R.string.nilai_persentase0), maxConfidence*100)
        textOutput1.text = classes[secondIdx]
        textScore1.text = String.format(getString(R.string.nilai_persentase0), second*100)
        textOutput2.text = classes[thirdIdx]
        textScore2.text = String.format(getString(R.string.nilai_persentase0), third*100)
//        Log.i("taged", "outputGenerator: $outputFeature0")
//        var zz=confidences.joinToString(", ", "[", "]")
//        Log.i("taged", "outputGenerator: $zz")
//        for (i in 0..classes.size){
//            var s = String.format()
//        }
//        textOutput1.text = highestProbability[1].label
//        textOutput2.text = highestProbability[2].label
//        textScore0.text = String.format(getString(R.string.nilai_persentase0), highestProbability[0].score*100)
//        textScore1.text = String.format(getString(R.string.nilai_persentase1), highestProbability[1].score*100)
//        textScore2.text = String.format(getString(R.string.nilai_persentase2), highestProbability[2].score*100)
//        Log.i("TAG", "outputGenerator: $x")

        // Runs model inference and gets result. Then sort it in descending order
//        val outputs = model.process(image)
//            .probabilityAsCategoryList.apply {
//                sortByDescending { it.score }
//            }

//        val probability = outputs.probabilityAsCategoryList
//        val highestProbability = outputs[0]
//        val highestProbability = outputs.take(3)
//
//        //set output text
////        textOutput.text = highestProbability.label
//        textOutput0.text = highestProbability[0].label
//        textOutput1.text = highestProbability[1].label
//        textOutput2.text = highestProbability[2].label
//        textScore0.text = String.format(getString(R.string.nilai_persentase0), highestProbability[0].score*100)
//        textScore1.text = String.format(getString(R.string.nilai_persentase1), highestProbability[1].score*100)
//        textScore2.text = String.format(getString(R.string.nilai_persentase2), highestProbability[2].score*100)
//        Log.i("TAG", "outputGenerator: $highestProbability")
        // Releases model resources if no longer used.
        model.close()
    }

    //to download image to device
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        isGranted: Boolean ->
        if (isGranted){
            AlertDialog.Builder(this).setTitle("Download Image>")
                .setMessage("Do you want to download this image?")
                .setPositiveButton("Yes"){_, _ ->
                    val drawable: BitmapDrawable = imageView.drawable as BitmapDrawable
                    val bitmap = drawable.bitmap
                    downloadImage(bitmap)
                }
                .setNegativeButton("No"){dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }else{
            Toast.makeText(this, "Please allow permission to download the image", Toast.LENGTH_LONG).show()
        }
    }

    //function that takes a bitmap and store to user's device
    private fun downloadImage(mBitmap: Bitmap):Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Birds_Images" + System.currentTimeMillis()/1000)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        if (uri != null){
            contentResolver.insert(uri, contentValues)?.also {
                contentResolver.openOutputStream(it).use{ outputStream ->
                    if (!mBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)){
                        throw IOException("Couldn't save the bitmap")
                    }else{
                        Toast.makeText(applicationContext, "Image saved", Toast.LENGTH_LONG).show()
                    }
                }
                return it
            }
        }
        return null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.theme -> {
                val i = Intent(this, SettingActivity::class.java)
                startActivity(i)
                true
            }
            R.id.about -> {
                val i = Intent(this, AboutActivity::class.java)
                startActivity(i)
                true
            }
            R.id.tutorial -> {
                val i = Intent(this, TutorialActivity::class.java)
                startActivity(i)
                true
            }
            else -> true
        }
    }


//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//
//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
    private fun showViews(){
        val viewIds = listOf(
            binding.textView,
            binding.textOutput0,
            binding.textScore0,
            binding.textOutput1,
            binding.textScore1,
            binding.textOutput2,
            binding.textScore2,
            binding.information1,
            binding.garis0,
            binding.garis1
        )

        binding.information0.visibility = View.GONE
        binding.information2.visibility = View.GONE
        binding.textHowToUse.visibility = View.GONE
        viewIds.forEach { id ->
            id.visibility = View.VISIBLE
        }
    }

    private fun applyThemeFromPreference() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themeValue = sharedPreferences.getString("theme_preference", "Terang")
        when (themeValue) {
            "Otomatis (Mengikuti tema perangkat)" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            "Gelap" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun textInstructions(){
//        val listText = arrayOf(R.string.steps_of_how_to_use)
        val listText = resources.getStringArray(R.array.steps_of_how_to_use)
        val builder = SpannableStringBuilder()
        for (i in listText.indices) {
            val line = listText[i]
            val lineNumber = (i + 1).toString()
            val numberedLine = "$lineNumber.  $line"
            val ss = SpannableString(numberedLine)
            ss.setSpan(
                LeadingMarginSpan.Standard(0, 50),
                0,
                numberedLine.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            builder.append(ss)
        }
        binding.information0.text = builder
    }

//        val builder = SpannableStringBuilder()
//        for (i in listText.indices){
//            val textNumber = (i+1).toString()
//            val stepText = listText[i]
//            val formattedText = builder.append("$textNumber. $stepText\n")
//
//            val spannable = SpannableString(formattedText)
//            spannable.setSpan(
//                LeadingMarginSpan.Standard(40), // Adjust the indentation as needed
//                0, formattedText.length,
//                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//            builder.append(spannable)
//        }

}