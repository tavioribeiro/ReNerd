package com.example.renerd.components.player

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.example.renerd.R
import com.example.renerd.core.utils.log
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CustomBottomSheet @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var onExpandedCallback: (() -> Unit)? = null
    private var onCollapsedCallback: (() -> Unit)? = null

    init {
        View.inflate(context, R.layout.bottom_sheet_layout, this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupBottomSheet {
            collapse()
        }
    }

    private fun setupBottomSheet(onInitialized: () -> Unit) {
        bottomSheetBehavior = BottomSheetBehavior.from(this)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.peekHeight = 200
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        setBackgroundColor(resources.getColor(R.color.blue))
                        onExpandedCallback?.invoke()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        setBackgroundColor(resources.getColor(R.color.green))
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
