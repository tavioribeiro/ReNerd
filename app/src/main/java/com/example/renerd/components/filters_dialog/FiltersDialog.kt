package com.example.renerd.components.filters_dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.renerd.R
import com.example.renerd.core.extentions.ContextManager
import com.example.renerd.databinding.CLayoutFilterModalBinding
import com.example.renerd.view_models.FiltersTabsListModel
import core.extensions.hexToArgb
import core.extensions.styleBackground

class FiltersDialog(
    private val context: Context,
    private val filtersList: FiltersTabsListModel,
    private val onSave: (FiltersTabsListModel) -> Unit
) : DialogFragment() {

    private lateinit var binding: CLayoutFilterModalBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CLayoutFilterModalBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mainContainer.background = ColorDrawable(hexToArgb(30, ContextManager.getColorHex(0)))

        binding.mainContainer.setOnClickListener(){
           this.dismissModal()
        }
        binding.boxContainer.setOnClickListener(){}


        binding.boxContainer.styleBackground(
            backgroundColor = ContextManager.getColorHex(1),
            radius = 40f
        )

/*
        binding.boxContainer.setOnClickListener(){
            binding.boxContainer.styleBackground(
                backgroundColor = ContextManager.getColorHex(6),
                radius = 40f
            )
        }
*/

        binding.saveButtom.setOnClickListener(){
            
        }

        val mainTabFragmentPhone = FiltersTabs.newInstance(filtersList)

        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContent, mainTabFragmentPhone)
            .commit()
    }





    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        dialog.window?.decorView?.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }

        return dialog
    }




    override fun onResume() {
        super.onResume()
        val window = dialog?.window
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    fun dismissModal() {
        dismiss()
    }
}