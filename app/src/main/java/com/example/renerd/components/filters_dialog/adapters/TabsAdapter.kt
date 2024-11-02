package com.example.renerd.components.filters_dialog.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.renerd.components.filters_dialog.tabs.FiltersGuestTabFragment
import com.example.renerd.components.filters_dialog.tabs.FiltersProductTabFragment
import com.example.renerd.components.filters_dialog.tabs.FiltersSubjectTabFragment
import com.example.renerd.components.filters_dialog.tabs.FiltersYearTabFragment
import com.example.renerd.core.utils.log
import com.example.renerd.view_models.FiltersTabsListModel


class TabsAdapter(
    private val context: Context,
    private val fragment: Fragment,
    private val size: Int,
    private val tabs: FiltersTabsListModel
) : FragmentStateAdapter(fragment) {

    private val filtersProductTabFragment = FiltersProductTabFragment(tabs.productsList)
    private val filtersSubjectTabFragment = FiltersSubjectTabFragment(tabs.subjectsList)
    private val filtersGuestTabFragment = FiltersGuestTabFragment(tabs.guestsList)
    private val filtersYearTabFragment = FiltersYearTabFragment(tabs.yearsList)



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

