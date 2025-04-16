package com.example.renerd.features.episodes.components.search_dialog

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.renerd.core.extentions.ContextManager
import com.example.renerd.databinding.CLayoutSearchModalBinding
import com.example.renerd.features.episodes.components.search_dialog.adapters.SearchEpisodesAdapter
import com.example.renerd.view_models.EpisodeViewModel
import core.extensions.hexToArgb
import core.extensions.styleBackground

class SearchDialog(
    private val context: Context,
    private val episodesList: List<EpisodeViewModel>,
    private val onSave: (MutableList<EpisodeViewModel>) -> Unit
) : DialogFragment(){

    private lateinit var binding: CLayoutSearchModalBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CLayoutSearchModalBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initView()
    }


    private fun initView() {
        this.styleCenterBox()
        this.setUpTitle()
        this.setUpRecyclerView()
        this.setyleInputBox()
    }


    private fun styleCenterBox(){
        binding.mainContainer.background = ColorDrawable(hexToArgb(30, ContextManager.getColorHex(0)))

        binding.mainContainer.setOnClickListener(){
            this.dismissModal()
        }
        binding.boxContainer.setOnClickListener(){}

        binding.boxContainer.styleBackground(
            backgroundColor = ContextManager.getColorHex(1),
            borderColor = ContextManager.getColorHex(2),
            borderWidth = 5,
            radius = 80f
        )
    }

    private fun setyleInputBox(){
        binding.seachInput.styleBackground(
            backgroundColor = ContextManager.getColorHex(2),
            radius = 12f,
            borderWidth = 0
        )
    }

    private fun setUpTitle(){
        binding.title.text = "Pesquisa"
    }



    private fun setUpRecyclerView(){
        binding.recyclerviewBase.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val searchEpisodesAdapter = SearchEpisodesAdapter(
            episodesList = episodesList,
            onClick = { filtersTabsListItemModel ->

            }
        )

        binding.recyclerviewBase.adapter = searchEpisodesAdapter
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
