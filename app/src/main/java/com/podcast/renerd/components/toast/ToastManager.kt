package com.podcast.renerd.components.toast

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.podcast.renerd.R
import com.podcast.renerd.core.extentions.styleBackground
import com.podcast.renerd.core.singletons.ColorsManager
import com.podcast.renerd.databinding.CLayoutToastBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.components.toast.ToastType

class ToastModalBFragment : DialogFragment() {

    private lateinit var binding: CLayoutToastBinding
    private var onFinishListener: (() -> Unit)? = null

    private var toastType: Int = ToastType.TYPE_INFO
    private var toastTitle: String? = null
    private var toastDescription: String? = null
    private var toastTime: Long = 7000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            toastType = it.getInt(ARG_TYPE, ToastType.TYPE_INFO)
            toastTitle = it.getString(ARG_TITLE)
            toastDescription = it.getString(ARG_DESCRIPTION)
            toastTime = it.getLong(ARG_TIME, 5000)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.attributes?.gravity = Gravity.TOP or Gravity.END
            val marginEnd = 0
            val marginTop = 80
            window?.attributes?.x = marginEnd
            window?.attributes?.y = marginTop
            window?.setDimAmount(0.0f)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CLayoutToastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToastStyle()
        animateOverlayIn()
        scheduleDismiss()
    }

    private fun setupToastStyle() {
        if (!isAdded || isDetached || activity == null || activity?.isFinishing == true || activity?.isDestroyed == true) {
            return
        }

        val tempBackgroundColor: String
        val tempIcon: Int
        val tempTitle: String
        val tempDescription: String
        val tempTextColor: String

        when (toastType) {
            ToastType.TYPE_INFO -> {
                tempBackgroundColor = "#AAC4FF"
                tempIcon = R.drawable.icon_info
                tempTitle = toastTitle ?: "Informação"
                tempDescription = toastDescription ?: ""
                tempTextColor = ColorsManager.getColorHex(1)
            }

            ToastType.TYPE_SUCCESS -> {
                tempBackgroundColor = "#02E26B"
                tempIcon = R.drawable.icon_sucess
                tempTitle = toastTitle ?: "Sucesso"
                tempDescription = toastDescription ?: "Operação realizada com sucesso!"
                tempTextColor = ColorsManager.getColorHex(1)
            }

            ToastType.TYPE_ERROR -> {
                tempBackgroundColor = "#D93F3F"
                tempIcon = R.drawable.icon_error
                tempTitle = toastTitle ?: "Erro"
                tempDescription = toastDescription ?: "Não foi possível concluir a operação!"
                tempTextColor = ColorsManager.getColorHex(5)
            }

            ToastType.TYPE_WARNING -> {
                tempBackgroundColor = "#FFAE00"
                tempIcon = R.drawable.icon_alert
                tempTitle = toastTitle ?: "Atenção"
                tempDescription = toastDescription ?: ""
                tempTextColor = ColorsManager.getColorHex(1)
            }

            else -> {
                tempBackgroundColor = "#AAC4FF"
                tempIcon = R.drawable.icon_info
                tempTitle = toastTitle ?: "Informação"
                tempDescription = toastDescription ?: ""
                tempTextColor = ColorsManager.getColorHex(1)
            }
        }

        if (isAdded && !isDetached && activity != null && !(activity?.isFinishing == true || activity?.isDestroyed == true)) {
            binding.mainContainer.styleBackground(
                backgroundColor = tempBackgroundColor,
                topLeftRadius = 4f,
                bottomLeftRadius = 4f
            )
            binding.icon.setImageResource(tempIcon)
            binding.icon.setColorFilter(Color.parseColor(tempTextColor))

            binding.title.text = tempTitle
            binding.title.setTextColor(Color.parseColor(tempTextColor))

            binding.description.text = tempDescription
            binding.description.setTextColor(Color.parseColor(tempTextColor))
            binding.description.isSelected = false
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) { // Use viewLifecycleOwner.lifecycleScope
                delay(2000)
                if (isAdded && !isDetached && activity != null && !(activity?.isFinishing == true || activity?.isDestroyed == true)) {
                    binding.description.isSelected = true
                }
            }
        }
    }

    private fun animateOverlayIn() {
        if (!isAdded || isDetached || activity == null || activity?.isFinishing == true || activity?.isDestroyed == true) {
            return // Avoid animation if fragment is not properly attached
        }
        binding.mainContainer.translationX = resources.displayMetrics.widthPixels.toFloat()
        val slideInAnim = ObjectAnimator.ofFloat(binding.mainContainer, "translationX", 0f)
        slideInAnim.duration = 500
        slideInAnim.interpolator = DecelerateInterpolator()
        slideInAnim.start()
    }

    private fun animateOverlayOut(onAnimationEnd: () -> Unit) {
        if (!isAdded || isDetached || activity == null || activity?.isFinishing == true || activity?.isDestroyed == true) {
            onAnimationEnd() // Directly trigger the end action if fragment is not valid
            return // Avoid animation if fragment is not properly attached
        }
        val slideOutAnim = ObjectAnimator.ofFloat(
            binding.mainContainer,
            "translationX",
            resources.displayMetrics.widthPixels.toFloat()
        )
        slideOutAnim.duration = 500
        slideOutAnim.interpolator = AccelerateInterpolator()
        slideOutAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (isAdded && !isDetached && activity != null && !(activity?.isFinishing == true || activity?.isDestroyed == true)) {
                    onAnimationEnd()
                }
            }
        })
        slideOutAnim.start()
    }

    private fun scheduleDismiss() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) { // Use viewLifecycleOwner.lifecycleScope
            delay(toastTime)
            if (isAdded && !isDetached && activity != null && !(activity?.isFinishing == true || activity?.isDestroyed == true)) {
                animateOverlayOut {
                    onFinishListener?.invoke()
                    if (isAdded && !isDetached && activity != null && !(activity?.isFinishing == true || activity?.isDestroyed == true)) {
                        super.dismiss()
                    }
                }
            } else {
                // Fragment/Activity is gone, just invoke listener and dismiss silently if possible.
                onFinishListener?.invoke()
                if (isAdded && !isDetached && activity != null && !(activity?.isFinishing == true || activity?.isDestroyed == true)) {
                    super.dismiss()
                }
            }
        }
    }

    override fun dismiss() {
        if (isAdded && !isDetached && activity != null && !(activity?.isFinishing == true || activity?.isDestroyed == true)) {
            animateOverlayOut {
                onFinishListener?.invoke()
                if (isAdded && !isDetached && activity != null && !(activity?.isFinishing == true || activity?.isDestroyed == true)) {
                    super.dismiss()
                }
            }
        } else {
            // Fragment/Activity is gone, just invoke listener and dismiss silently if possible.
            onFinishListener?.invoke()
            if (isAdded && !isDetached && activity != null && !(activity?.isFinishing == true || activity?.isDestroyed == true)) {
                super.dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        private const val ARG_TYPE = "toast_type"
        private const val ARG_TITLE = "toast_title"
        private const val ARG_DESCRIPTION = "toast_description"
        private const val ARG_TIME = "toast_time"
        private const val ARG_ON_FINISH = "toast_on_finish"

        fun newInstance(
            type: Int = ToastType.TYPE_INFO,
            title: String? = null,
            description: String? = null,
            time: Long = 5000,
            onFinish: (() -> Unit)? = null
        ): ToastModalBFragment {
            val args = Bundle().apply {
                putInt(ARG_TYPE, type)
                putString(ARG_TITLE, title)
                putString(ARG_DESCRIPTION, description)
                putLong(ARG_TIME, time)
            }
            return ToastModalBFragment().apply {
                arguments = args
                onFinishListener = onFinish
            }
        }
    }
}

class ToastManager(private val activity: FragmentActivity) {

    fun showToast(
        type: Int = ToastType.TYPE_INFO,
        title: String? = null,
        description: String? = null,
        time: Long = 5000,
        onFinish: () -> Unit = {}
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onFinish()
            return
        }
        val modalBFragment = ToastModalBFragment.newInstance(type, title, description, time, onFinish)
        modalBFragment.show(activity.supportFragmentManager, "ToastModalBTag")
    }
}