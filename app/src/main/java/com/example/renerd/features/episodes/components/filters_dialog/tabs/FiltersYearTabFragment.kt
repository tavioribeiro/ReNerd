package com.example.renerd.features.episodes.components.filters_dialog.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.renerd.features.episodes.components.filters_dialog.FilterTabListener
import com.example.renerd.features.episodes.components.filters_dialog.adapters.FilterItemAdapter
import com.example.renerd.databinding.CLayoutFilterTabBinding
import com.example.renerd.view_models.FiltersTabsItemModel


class FiltersYearTabFragment(
    private val yearsList: List<FiltersTabsItemModel>,
    private val filterTabListener: FilterTabListener
) : Fragment() {

    private lateinit var binding: CLayoutFilterTabBinding

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
    }




    private fun setUpTitle(){
        binding.title.text = "Filtro por Ano"
    }




    private fun setUpRecyclerView(){
        binding.recyclerviewBase.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val filterItemAdapter = FilterItemAdapter(
            filtersTabsListItemModel = yearsList,
            onClick = { filtersTabsListItemModel ->
                filterTabListener.onItemValeuChange(filtersTabsListItemModel)
            }
        )

        binding.recyclerviewBase.adapter = filterItemAdapter
    }




    companion object {
        fun newInstance(yearsList: List<FiltersTabsItemModel>, filterTabListener: FilterTabListener): FiltersYearTabFragment {
            return FiltersYearTabFragment(yearsList, filterTabListener)
        }
    }
}