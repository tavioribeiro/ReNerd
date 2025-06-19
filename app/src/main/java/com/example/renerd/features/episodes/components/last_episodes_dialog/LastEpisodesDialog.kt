package com.example.renerd.features.episodes.components.last_episodes_dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.renerd.core.extentions.ContextManager
import com.example.renerd.core.utils.log
import com.example.renerd.databinding.CLayoutLastEpisodesModalBinding
import com.example.renerd.features.episodes.EpisodesContract
import com.example.renerd.features.episodes.components.last_episodes_dialog.adapters.LastEpisodesEpisodesAdapter
import com.example.renerd.view_models.EpisodeViewModel
import core.extensions.hexToArgb
import core.extensions.styleBackground
import org.koin.android.ext.android.inject

class LastEpisodesDialog(
    private val context: Context,
    private val onClick: (EpisodeViewModel) -> Unit
) : DialogFragment(), LastEpisodesDialogContract.View {

    private lateinit var binding: CLayoutLastEpisodesModalBinding
    private val presenter: LastEpisodesDialogContract.Presenter by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CLayoutLastEpisodesModalBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        this.initView()
    }


    private fun initView() {
        this.styleCenterBox()
        this.setUpTitle()
        //this.setUpRecyclerView()
        this.setyleInputBox()
        this.inputTypeMonit()
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
            borderWidth = 1,
            borderColor = ContextManager.getColorHex(5)
        )
    }

    private fun inputTypeMonit(){
        binding.seachInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                presenter.lastEpisodesEpisodesByName(text)
            }

            override fun afterTextChanged(editable: Editable?) { }
        })
    }


    private fun setUpTitle(){
        binding.title.text = "Pesquisa"
    }



    private fun setUpRecyclerView(episodesList: List<EpisodeViewModel>){
        binding.recyclerviewBase.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val lastEpisodesEpisodesAdapter = LastEpisodesEpisodesAdapter(
            resources = resources,
            episodesList = episodesList,
            onClick = { filtersTabsListItemModel ->

            }
        )

        binding.recyclerviewBase.adapter = lastEpisodesEpisodesAdapter
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

    override fun showLoading() {
        //TODO("Not yet implemented")
    }

    override fun hideLoading() {
        //TODO("Not yet implemented")
    }

    override fun showError(message: String) {
        //TODO("Not yet implemented")
    }

    override fun showEpisodes(episodes: List<EpisodeViewModel>, currentPosition: Int) {
        binding.recyclerviewBase.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val lastEpisodesEpisodesAdapter = LastEpisodesEpisodesAdapter(
            resources = resources,
            episodesList = episodes,
            onClick = { episode ->
                onClick(episode)
            }
        )

        binding.recyclerviewBase.adapter = lastEpisodesEpisodesAdapter
    }
}
