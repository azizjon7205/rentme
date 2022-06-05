package com.rentme.rentme.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rentme.rentme.R
import com.rentme.rentme.databinding.ActivityLoginBinding.inflate
import com.rentme.rentme.databinding.BottomSheetLocaliztionBinding
import com.rentme.rentme.databinding.BottomSheetLogOutBinding

class BottomSheetFragmentLogOut:BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetLogOutBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetLogOutBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
           binding.btnYes.setOnClickListener {
               Toast.makeText(requireContext(), "Go to Sign in Page", Toast.LENGTH_SHORT).show()
           }
        binding.btnNo.setOnClickListener {
            Toast.makeText(requireContext(), "Log out cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}