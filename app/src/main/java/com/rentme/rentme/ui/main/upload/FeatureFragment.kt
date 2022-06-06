package com.rentme.rentme.ui.main.upload

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rentme.rentme.R
import com.rentme.rentme.adapter.CarImageAdapter
import com.rentme.rentme.adapter.ColorAdapter
import com.rentme.rentme.databinding.FragmentFeaturesBinding
import com.rentme.rentme.model.UploadAdvertisement
import com.rentme.rentme.utils.SelectColor
import com.rentme.rentme.utils.UiStateList
import com.sangcomz.fishbun.FishBun
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter
import java.io.File
import java.io.FileOutputStream
import java.time.Year


// val date = SimpleDateFormat("dd-MM-yyyy").parse(binding.tvDate.text.toString())

class FeatureFragment : Fragment() {
    private val viewModel by viewModels<FeatureViewModel> ()

    private val colorAdapter by lazy { ColorAdapter() }
    private lateinit var carImageAdapter: CarImageAdapter
    private val TAG = requireContext() :: class.java.simpleName

    private var _binding: FragmentFeaturesBinding? = null
    private val binding get() = _binding!!

    private var uploadAdvertisement: UploadAdvertisement? = null
    private var allPhotos: ArrayList<Uri> = ArrayList()
    private var carImages: ArrayList<Uri> = ArrayList()
    private var carImageUrls: ArrayList<String> = ArrayList()
    private var selectModelName: String = ""
    private var selectColorName: String = ""
    private var selectYear: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uploadAdvertisement = it.getSerializable("uploadAdvertisement") as UploadAdvertisement
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFeaturesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvStartDate.text = uploadAdvertisement?.startDate.toString()
        if (uploadAdvertisement?.minDuration!! < 30){
            binding.minDayCount.text = uploadAdvertisement?.minDuration.toString()
            binding.minTypeTime.text = getString(R.string.str_days)
        }
        else{
            binding.minDayCount.text = (uploadAdvertisement?.minDuration!! / 30).toString()
            binding.minTypeTime.text = getString(R.string.str_months)
        }

        selectModelSpinner()
        selectYearSpinner()
        allColorFunction()
        enterPriceFunction()

        binding.btnSave.setOnClickListener {
            openMyAddsFragment()
        }
        carImageAdapter = CarImageAdapter()
        binding.rvCarPhotos.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL, false)
        binding.rvCarPhotos.adapter = carImageAdapter
        binding.ivAddPhoto.setOnClickListener { pickFishBunCarImages() }

        setupObservers()

    }

    private fun setupObservers(){
        lifecycleScope.launchWhenCreated {
            viewModel.fileState.collect{
                when(it){
                    is UiStateList.LOADING -> {}
                    is UiStateList.SUCCESS -> {
                        carImageUrls.addAll(it.data)
                        carImageAdapter.items.clear()
                        carImageAdapter.saveCarImageStorage(carImages)
                    }
                    is UiStateList.ERROR ->{
                        Log.d(TAG, it.message) }
                    else -> Unit
                }
            }
        }



    }

    private fun openMyAddsFragment(){
        var prices = ((binding.llDailyPrice.visibility == View.VISIBLE && binding.edtPriceDaily.text.isNotEmpty())
                || binding.llDailyPrice.visibility == View.GONE)
                && ((binding.llMonthlyPrice.visibility == View.VISIBLE && binding.edtPriceMonthly.text.isNotEmpty())
                || binding.llMonthlyPrice.visibility == View.GONE)
        if (prices && carImageUrls.isNotEmpty() && carImageUrls.size > 1){

            findNavController().navigate(R.id.myAddsFragment)
        }else{
            Toast.makeText(requireContext(), getString(R.string.str_fill_all_fields), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Pick photo using FishBun library
     */
    private fun pickFishBunCarImages(){
        FishBun.with(this)
            .setImageAdapter(GlideAdapter())
            .setMaxCount(10)
            .setMinCount(1)
            .setCamera(true)
            .setSelectedImages(allPhotos)
            .startAlbumWithActivityResultCallback(photoLauncher)
    }

    @SuppressLint("NotifyDataSetChanged")
    private val photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK){
            allPhotos = it.data?.getParcelableArrayListExtra(FishBun.INTENT_PATH) ?: arrayListOf()
            carImageAdapter.items.addAll(allPhotos)
            carImages.addAll(allPhotos)

            val imageFiles = getFileList(allPhotos)
            viewModel.createFile(imageFiles)

            allPhotos.clear()
            carImageAdapter.notifyDataSetChanged()
        }
    }

    private fun enterPriceFunction(){
        if (uploadAdvertisement?.minDuration!! < 30) binding.llDailyPrice.visibility = View.VISIBLE
        else binding.llDailyPrice.visibility = View.GONE

        if (uploadAdvertisement?.maxDuration!! > 30) binding.llMonthlyPrice.visibility = View.VISIBLE
        else binding.llMonthlyPrice.visibility = View.GONE
    }

    private fun allColorFunction(){
        val colors: ArrayList<Int> = ArrayList()
        colors.add(R.color.car_black)
        colors.add(R.color.car_meteor_grey)
        colors.add(R.color.car_bright_white)
        colors.add(R.color.car_candy_white)
        colors.add(R.color.car_brilliant_silver)
        colors.add(R.color.car_energy_blue)
        colors.add(R.color.car_race_blue)
        colors.add(R.color.car_velvet_red)
        colors.add(R.color.car_corrida_red)
        colors.add(R.color.car_yellow)
        colors.add(R.color.car_orange)
        colorAdapter.submitData(colors)

        binding.rvColors.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvColors.adapter = colorAdapter
        colorAdapter.onClick = {color ->
            selectColorName = SelectColor.codeToName(color)
            binding.tvColorName.text = selectColorName
        }
    }

    private fun selectModelSpinner(){
        val models: ArrayList<String> = ArrayList()
        models.add("Malibu")
        models.add("Captiva")
        models.add("Nexia")
        models.add("Spark")
        models.add("Matiz")
        models.add("Damas")

        binding.spnModels.adapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_item_view, models)
        binding.spnModels.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectModelName = p0!!.getItemAtPosition(p2).toString()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectYearSpinner(){
        val currentYear = Year.now().value
        val years: ArrayList<String> = ArrayList()
        for (i in currentYear downTo 2000){
            years.add(i.toString())
        }
        binding.spnYear.adapter = ArrayAdapter<String>(requireContext(), R.layout.spinner_item_view, years)
        binding.spnYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectYear = p0!!.getItemAtPosition(p2).toString()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun getManagementSystem(): String{
        val selectedId: Int = binding.systemRadioGroup.checkedRadioButtonId
        if (selectedId == R.id.system_radio_mechanical) return "MECHANICAL"
        return "AUTOMATIC"
    }

    private fun getFuelType(): String{
        val selectedId: Int = binding.fuelRadioGroup.checkedRadioButtonId
        if (selectedId == R.id.fuel_radio_petrol) return "PETROL"
        return "PETROL/GAS"
    }

    private fun checkAdditional() : Boolean{
        return (binding.chbConditioners.isChecked || binding.chbRadio.isChecked)
    }

    private fun getFileList(uris: ArrayList<Uri>) : ArrayList<File>{
        val files: ArrayList<File> = ArrayList()
        for (uri in uris){
            files.add(getFile(uri))
        }
        return files
    }

    private fun getFile(uri: Uri): File {
        val ins = requireContext().contentResolver.openInputStream(uri)

        val file = File.createTempFile("file", ".jpg",
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

        val fileOutputStream = FileOutputStream(file)
        ins?.copyTo(fileOutputStream)
        ins?.close()
        fileOutputStream.close()
        return file
    }

}