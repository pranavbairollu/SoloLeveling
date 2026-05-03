package com.example.sololeveling.ui.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.sololeveling.R

class SystemNotificationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var container: View
    private var tvMessage: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var isAnimating = false

    enum class Type {
        INFO, WARNING, SUCCESS, MONARCH
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_system_notification, this, true)
        container = view.findViewById(R.id.notification_container)
        tvMessage = view.findViewById(R.id.tv_message)
        
        // Initial state: hidden above screen
        this.visibility = View.GONE
        container.translationY = -200f // Start above
    }

    private var currentType: Type? = null
    private var currentMessage: String? = null

    fun show(message: String, type: Type) {
        // Jitter Prevention: If same message and type is already showing, do nothing
        if (visibility == View.VISIBLE && currentMessage == message && currentType == type) {
            return
        }

        // Priority Logic: MONARCH > WARNING > everything else.
        if (visibility == View.VISIBLE) {
            if (currentType == Type.MONARCH && type != Type.MONARCH) return
            if (currentType == Type.WARNING && type == Type.INFO) return 
        }

        // Cancel any pending dismiss
        handler.removeCallbacksAndMessages(null)
        
        currentType = type
        currentMessage = message
        
        // Apply Style
        val bgRes = when (type) {
            Type.INFO -> R.drawable.bg_notification_info
            Type.WARNING -> R.drawable.bg_notification_warning
            Type.SUCCESS -> R.drawable.bg_notification_success
            Type.MONARCH -> R.drawable.bg_notification_success // Reuse gold-base if available
        }
        
        container.setBackgroundResource(bgRes)
        
        if (type == Type.MONARCH) {
            tvMessage.setTextColor(android.graphics.Color.parseColor("#FFD700")) // Gold
            container.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#800080")) // Purple/Magenta base
        } else {
            val textColor = when (type) {
                Type.INFO -> R.color.system_neon_blue
                Type.WARNING -> R.color.system_danger_red
                Type.SUCCESS -> R.color.system_neon_gold
                else -> R.color.system_neon_gold
            }
            tvMessage.setTextColor(ContextCompat.getColor(context, textColor))
            container.backgroundTintList = null
        }
        
        if (visibility == View.VISIBLE) {
             // Slide up out -> Slide down in for fresh impact
             animateOut {
                 startEntryAnimation(message)
             }
        } else {
            startEntryAnimation(message)
        }
    }

    private fun startEntryAnimation(message: String) {
        visibility = View.VISIBLE
        container.translationY = -container.height.toFloat().coerceAtLeast(200f) // Ensure offscreen
        container.alpha = 0f
        
        container.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(250)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Start Typewriter AFTER entry
                    AnimUtils.typewriter(tvMessage, message, delayMs = 20) {
                        // Queue Dismiss
                        handler.postDelayed({ dismiss() }, 2500)
                    }
                }
            })
            .start()
    }
    
    fun dismiss() {
        animateOut()
    }

    private fun animateOut(onComplete: (() -> Unit)? = null) {
        container.animate()
            .translationY(-container.height.toFloat())
            .alpha(0f)
            .setDuration(200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                    onComplete?.invoke()
                }
            })
            .start()
    }
}
