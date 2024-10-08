package com.example.renerd.components.player

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.renerd.R
import com.example.renerd.core.utils.log
import com.example.renerd.databinding.BottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CustomBottomSheet @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var onExpandedCallback: (() -> Unit)? = null
    private var onCollapsedCallback: (() -> Unit)? = null
    private val binding: BottomSheetLayoutBinding = BottomSheetLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        //View.inflate(context, R.layout.bottom_sheet_layout, this) -
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.setupBottomSheet {
            collapse()
        }
        this.setUpTouch()
    }

    private fun setupBottomSheet(onInitialized: () -> Unit) {
        bottomSheetBehavior = BottomSheetBehavior.from(this)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.peekHeight = 200
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.root.setBackgroundColor(resources.getColor(R.color.blue))
                        onExpandedCallback?.invoke()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.root.setBackgroundColor(resources.getColor(R.color.green))
                        onCollapsedCallback?.invoke()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                log("abertura: ${slideOffset * 100}%")
            }
        })

        onInitialized()
    }

    private fun setUpTouch(){
        binding.pink.setOnClickListener {
            this.expand()
        }
    }

    fun expand() {
        log("expandidooo")
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun setOnExpandedCallback(callback: () -> Unit) {
        this.onExpandedCallback = callback
    }

    fun collapse() {
        log("Colapsouuu")
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun setOnCollapsedCallback(callback: () -> Unit) {
        this.onCollapsedCallback = callback
    }
}