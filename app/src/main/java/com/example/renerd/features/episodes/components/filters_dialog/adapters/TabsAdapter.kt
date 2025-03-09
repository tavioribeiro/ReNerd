package com.example.renerd.features.episodes.components.filters_dialog.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.renerd.features.episodes.components.filters_dialog.FilterTabListener
import com.example.renerd.features.episodes.components.filters_dialog.tabs.FiltersGuestTabFragment
import com.example.renerd.features.episodes.components.filters_dialog.tabs.FiltersProductTabFragment
import com.example.renerd.features.episodes.components.filters_dialog.tabs.FiltersSubjectTabFragment
import com.example.renerd.features.episodes.components.filters_dialog.tabs.FiltersYearTabFragment
import com.example.renerd.view_models.FiltersTabsListModel


class TabsAdapter(
    private val context: Context,
    private val fragment: Fragment,
    private val filterTabListener: FilterTabListener,
    private val size: Int,
    private var originalTabs: FiltersTabsListModel
) : FragmentStateAdapter(fragment) {

    val filtersProductTabFragment = FiltersProductTabFragment(originalTabs.productsList, filterTabListener)
    val filtersSubjectTabFragment = FiltersSubjectTabFragment(originalTabs.subjectsList, filterTabListener)
    val filtersGuestTabFragment = FiltersGuestTabFragment(originalTabs.guestsList, filterTabListener)
    val filtersYearTabFragment = FiltersYearTabFragment(originalTabs.yearsList, filterTabListener)



    override fun getItemCount(): Int {
        return size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> filtersProductTabFragment
            1 -> filtersSubjectTabFragment
            2 -> filtersGuestTabFragment
            3 -> filtersYearTabFragment
            else -> throw IllegalArgumentException("Posição inválida.")
        }
    }
}

