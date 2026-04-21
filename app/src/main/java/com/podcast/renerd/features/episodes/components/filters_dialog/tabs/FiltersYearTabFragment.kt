package com.podcast.renerd.features.episodes.components.filters_dialog.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.podcast.renerd.R
import com.podcast.renerd.features.episodes.components.filters_dialog.FilterTabListener
import com.podcast.renerd.features.episodes.components.filters_dialog.adapters.FilterItemAdapter
import com.podcast.renerd.databinding.CLayoutFilterTabBinding
import com.podcast.renerd.view_models.FiltersTabsItemModel


class FiltersYearTabFragment(
    private val yearsList: List<FiltersTabsItemModel>,
    private val filterTabListener: FilterTabListener
) : Fragment() {

    private lateinit var binding: CLayoutFilterTabBinding
    private lateinit var filterItemAdapter: FilterItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CLayoutFilterTabBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.initView()
    }


    private fun initView() {
        this.setUpTitle()
        this.setUpRecyclerView()
        this.setUpToggleButton()
    }


    private fun setUpTitle(){
        binding.title.text = "Filtro por Ano"
    }


    private fun setUpRecyclerView(){
        binding.recyclerviewBase.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        filterItemAdapter = FilterItemAdapter(
            filtersTabsListItemModel = yearsList,
            onClick = { filtersTabsListItemModel ->
                filterTabListener.onItemValeuChange(filtersTabsListItemModel)
            }
        )

        binding.recyclerviewBase.adapter = filterItemAdapter
    }

    private fun setUpToggleButton() {
        updateToggleButton()
        binding.buttonToggleAll.setOnClickListener {
            filterItemAdapter.selectAll(!filterItemAdapter.areAllSelected())
            updateToggleButton()
        }
    }

    private fun updateToggleButton() {
        if (filterItemAdapter.areAllSelected()) {
            binding.buttonToggleAll.setLabel("Desmarcar todos")
            binding.buttonToggleAll.setIconResource(R.drawable.icon_stop)
        } else {
            binding.buttonToggleAll.setLabel("Selecionar todos")
            binding.buttonToggleAll.setIconResource(R.drawable.icon_check)
        }
    }




    companion object {
        fun newInstance(yearsList: List<FiltersTabsItemModel>, filterTabListener: FilterTabListener): FiltersYearTabFragment {
            return FiltersYearTabFragment(yearsList, filterTabListener)
        }
    }
}