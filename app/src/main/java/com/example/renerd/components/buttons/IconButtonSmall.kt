package com.example.renerd.components.buttons

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import com.example.renerd.R
import com.example.renerd.core.extentions.dpToPx
import com.example.renerd.core.extentions.fadeInAnimationNoRepeat
import com.example.renerd.core.extentions.fadeOutAnimationNoRepeat
import com.example.renerd.core.extentions.gone
import com.example.renerd.core.extentions.show
import com.example.renerd.core.extentions.styleBackground
import com.example.renerd.core.singletons.ColorsManager
import com.example.renerd.databinding.LayoutIconButtonSmallBinding
import core.extensions.lightenColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class IconButtonSmall @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    @DrawableRes private var icon: Int? = null,
    private var action: () -> Unit = {},
    private var width: Float? = null,
    private var default_width: Boolean ?= null,
    private var backgroundColor: String? = null,
    private var borderColorOnFocus: String? = null,
    private var iconColor: String? = null,
    private var blockActions: MutableList<Int> = mutableListOf(),
    private var nextFocusLeftId: Int = 0,
    private var nextFocusRightId: Int = 0,
    private var nextFocusUpId: Int = 0,
    private var nextFocusDownId: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutIconButtonSmallBinding = LayoutIconButtonSmallBinding.inflate(LayoutInflater.from(context), this, true)
    private var onClickListener: (() -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())

    private var isFocused = false
    private var isClicked = false

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.DefaultButton,
            0, 0
        ).apply {
            try {
                // Prioriza valores do construtor, caso não definidos, usa os do XML
                icon = icon ?: getResourceId(R.styleable.DefaultButton_icon, 0)
                width = width ?: getFloat(R.styleable.DefaultButton_width, 0f)
                default_width = default_width ?: getBoolean(R.styleable.DefaultButton_default_width, true)
                backgroundColor = backgroundColor ?: getString(R.styleable.DefaultButton_bgdColor) ?: ColorsManager.getColorHex(0)
                borderColorOnFocus = borderColorOnFocus ?: getString(R.styleable.DefaultButton_borderColorOnFocus) ?: ColorsManager.getColorHex(5)
                iconColor = iconColor ?: getString(R.styleable.DefaultButton_iconColor)
                nextFocusLeftId = if (nextFocusLeftId == 0) getResourceId(R.styleable.DefaultButton_nextFocusLeft, 0) else nextFocusLeftId
                nextFocusRightId = if (nextFocusRightId == 0) getResourceId(R.styleable.DefaultButton_nextFocusRight, 0) else nextFocusRightId
                nextFocusUpId = if (nextFocusUpId == 0) getResourceId(R.styleable.DefaultButton_nextFocusUp, 0) else nextFocusUpId
                nextFocusDownId = if (nextFocusDownId == 0) getResourceId(R.styleable.DefaultButton_nextFocusDown, 0) else nextFocusDownId
                val blockActionsString = getString(R.styleable.DefaultButton_blockActions)
                blockActionsString?.split(",")?.map { it.trim().toIntOrNull() }?.filterNotNull()?.let {
                    if (blockActions.isEmpty()) blockActions.addAll(it)
                }
            } finally {
                recycle()
            }
        }

        if(backgroundColor == "#0") backgroundColor = "#00000000"

        this.setupComponent()
    }


    private fun setupComponent() {
        this.configureIcon()
        this.configureBackground()
        this.configureClickAction()
    }


    private fun configureIcon() {
        binding.imageViewIcon.apply {
            setImageResource(icon!!)
            setColorFilter(getIconColor())
        }
    }


    private fun getIconColor(): Int {
        return if (iconColor == null) {
            Color.parseColor(backgroundColor?.let { lightenColor(it, 70.0) })
        } else {
            Color.parseColor(iconColor)
        }
    }


    private fun configureBackground() {
        if(isClicked){
            binding.mainContainer.styleBackground(
                backgroundColor = ColorsManager.getColorHex(3),
                radius = 500f
            )
        }
        else{
            binding.mainContainer.styleBackground(
                backgroundColor = backgroundColor,
                radius = 0f
            )
        }
    }



    private fun configureClickAction() {
        binding.mainContainer.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                isClicked = true
                this@IconButtonSmall.configureBackground()

                delay(100)

                isClicked = false
                this@IconButtonSmall.configureBackground()

                action()
                onClickListener?.invoke()
            }
        }
    }


    fun setOnClickListener(listener: () -> Unit) {
        this.onClickListener = listener
    }


    fun showLoading(show: Boolean){
        if (show){
            binding.mainContainer.isClickable = false

            binding.imageViewIcon.fadeOutAnimationNoRepeat(200){
                binding.imageViewIcon.gone()
                binding.loading.fadeInAnimationNoRepeat(200)
            }
        }
        else{
            binding.loading.fadeOutAnimationNoRepeat(200){
                binding.loading.gone()
                binding.imageViewIcon.fadeInAnimationNoRepeat(200){
                    binding.imageViewIcon.show()
                }

                binding.mainContainer.isClickable = true
            }
        }
    }



    //Atualiza a margem esquerda do texto
    private fun View.updateLeftMargin(marginDp: Float) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.let {
            it.leftMargin = context.dpToPx(marginDp)
            layoutParams = it
        }
    }

    //Retorna o ID do componente
    fun getComponentId(): Int = binding.mainContainer.id



    //Atualiza o ícone do botão
    fun setIconResource(@DrawableRes newIcon: Int?) {
        icon = newIcon
        configureIcon()
    }


    //Atualiza a cor do ícone
    fun setIconColor(newColor: String?) {
        iconColor = newColor
        configureIcon()
    }

    //Atualiza a cor de fundo
    fun setBackgroundColor(newColor: String) {
        backgroundColor = newColor
        configureBackground()
    }


    /*companion object {
        private const val DEFAULT_BUTTON_WIDTH_DP = 60f
        private const val DEFAULT_BUTTON_HEIGHT_DP = 60f
    }*/
}