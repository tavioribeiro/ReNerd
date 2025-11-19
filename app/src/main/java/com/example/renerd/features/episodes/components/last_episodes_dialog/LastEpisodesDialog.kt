package com.example.renerd.features.episodes.components.last_episodes_dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.renerd.components.toast.ToastManager
import com.example.renerd.core.extentions.styleBackground
import com.example.renerd.core.singletons.ColorsManager
import com.example.renerd.databinding.CLayoutLastEpisodesModalBinding
import com.example.renerd.features.episodes.components.last_episodes_dialog.adapters.LastEpisodesEpisodesAdapter
import com.example.renerd.view_models.EpisodeViewModel
import core.extensions.hexToArgb
import org.koin.android.ext.android.inject
import ui.components.toast.ToastType

@RequiresApi(Build.VERSION_CODES.O)
class LastEpisodesDialog(
    private val context: Context,
    private val onClick: (EpisodeViewModel) -> Unit
) : DialogFragment(), LastEpisodesDialogContract.View {

    private lateinit var binding: CLayoutLastEpisodesModalBinding
    private val presenter: LastEpisodesDialogContract.Presenter by inject()

    private var newEpisodesList: List<EpisodeViewModel> = emptyList()
    private lateinit var lastEpisodesAdapter: LastEpisodesEpisodesAdapter

    private lateinit var toastManager: ToastManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CLayoutLastEpisodesModalBinding.inflate(inflater, container, false)
        presenter.attachView(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initView()

        toastManager = ToastManager(requireActivity())

        presenter.loadNewEpisodes()
    }

    private fun initView() {
        this.styleCenterBox()
        this.setUpTitle()
        this.setupRecyclerView()
        this.buttonMonit()
    }

    private fun styleCenterBox(){
        binding.mainContainer.background = hexToArgb(30, ColorsManager.getColorHex(0)).toDrawable()
        binding.mainContainer.setOnClickListener { this.dismissModal() }
        binding.boxContainer.setOnClickListener {}
        binding.boxContainer.styleBackground(
            backgroundColor = ColorsManager.getColorHex(1),
            borderColor = ColorsManager.getColorHex(2),
            borderWidth = 5,
            radius = 80f
        )
    }

    private fun buttonMonit(){
        /*binding.saveButtom.setOnClickListener {
            presenter.onSaveButtonClicked(newEpisodesList)
        }*/
    }

    private fun setUpTitle(){
        binding.title.text = "Últimos Episódios"
    }

    private fun setupRecyclerView() {

    }

    override fun showLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.recyclerviewBase.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
    }

    override fun showFeedbackMessage(message: String) {
        toastManager.showToast(
            type = ToastType.TYPE_SUCCESS,
            title = "Opss \uD83E\uDEE4",
            description = message,
            time = 5000,
            onFinish = {
                this.dismissModal()
            }
        )

    }

    override fun displayNewEpisodes(episodes: List<EpisodeViewModel>) {
        this.newEpisodesList = episodes

        binding.recyclerviewBase.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        lastEpisodesAdapter = LastEpisodesEpisodesAdapter(
            resources = resources,
            episodesList = episodes,
            onClick = { episode ->
                onClick(episode)
            }
        )
        binding.recyclerviewBase.adapter = lastEpisodesAdapter
    }

    override fun setSaveButtonEnabled(isEnabled: Boolean) {
        //binding.saveButtom.isEnabled = isEnabled
        //binding.saveButtom.alpha = if (isEnabled) 1.0f else 0.5f
    }

    override fun closeView() {
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }

    fun dismissModal() {
        dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        return dialog
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}