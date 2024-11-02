package com.example.renerd.components.filters_dialog

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.renerd.R
import com.example.renerd.components.filters_dialog.adapters.TabsAdapter
import com.example.renerd.core.extentions.ContextManager
import com.example.renerd.databinding.FragmentTabListHeadBinding
import com.example.renerd.view_models.FiltersTabsListModel
import core.extensions.hexToArgb
import core.extensions.styleBackground


class FiltersTabs(
    private val tabs: FiltersTabsListModel,
): Fragment() {

    private lateinit var binding: FragmentTabListHeadBinding
    private lateinit var tabsAdapter: TabsAdapter



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTabListHeadBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }




    private fun initView() {
        this.setUpInitialSelectedTabStyle()
        this.setUpTabs()
        this.setUpClickListeners()
        this.setUpViewPagerListener()
    }



    private fun setUpInitialSelectedTabStyle(){
        binding.textViewButtonProduct.styleBackground(
            backgroundColor = ContextManager.getColorHex(2),
            radius = 50f
        )
    }


    private fun setUpTabs(){
        tabsAdapter = TabsAdapter(this, requireContext(), 4, tabs)
        binding.viewPager.adapter = tabsAdapter
    }



    private fun setUpClickListeners(){
        binding.textViewButtonProduct.setOnClickListener(){
            this.setDefaultStyle()
            binding.textViewButtonProduct.styleBackground(
                backgroundColor = ContextManager.getColorHex(2),
                radius = 50f
            )

            binding.viewPager.setCurrentItem(0, true)
        }

        binding.textViewButtonSubject.setOnClickListener(){
            this.setDefaultStyle()
            binding.textViewButtonSubject.styleBackground(
                backgroundColor = ContextManager.getColorHex(2),
                radius = 50f
            )

            binding.viewPager.setCurrentItem(1, true)
        }

        binding.textViewButtonGuest.setOnClickListener(){
            this.setDefaultStyle()
            binding.textViewButtonGuest.styleBackground(
                backgroundColor = ContextManager.getColorHex(2),
                radius = 50f
            )

            binding.viewPager.setCurrentItem(2, true)
        }

        binding.textViewButtonYear.setOnClickListener(){
            this.setDefaultStyle()
            binding.textViewButtonYear.styleBackground(
                backgroundColor = ContextManager.getColorHex(2),
                radius = 50f
            )

            binding.viewPager.setCurrentItem(3, true)
        }
    }


    private fun setUpViewPagerListener(){
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                /*when(position){
                    0 -> {
                        setDefaultStyle()
                        binding.textViewButtonProduct.styleBackground(
                            backgroundColor = ContextManager.getColorHex(0)
                        )
                    }
                    1 -> {
                        setDefaultStyle()
                        binding.textViewButtonProduct.styleBackground(
                            backgroundColor = ContextManager.getColorHex(0)
                        )
                    }
                    2 -> {
                        setDefaultStyle()
                        binding.textViewButtonProduct.styleBackground(
                            backgroundColor = ContextManager.getColorHex(0)
                        )
                    }
                    3 -> {
                        setDefaultStyle()
                        binding.textViewButtonProduct.styleBackground(
                            backgroundColor = ContextManager.getColorHex(0)
                        )
                    }
                }*/
            }
        })
    }




    private fun setDefaultStyle(){
        binding.textViewButtonProduct.styleBackground(
            backgroundColor = ContextManager.getColorHex(0)
        )
        binding.textViewButtonSubject.styleBackground(
            backgroundColor = ContextManager.getColorHex(0)
        )
        binding.textViewButtonGuest.styleBackground(
            backgroundColor = ContextManager.getColorHex(0)
        )
        binding.textViewButtonYear.styleBackground(
            backgroundColor = ContextManager.getColorHex(0)
        )
    }


    companion object {
        fun newInstance(tabs: FiltersTabsListModel): FiltersTabs {
            return FiltersTabs(tabs)
        }
    }
}