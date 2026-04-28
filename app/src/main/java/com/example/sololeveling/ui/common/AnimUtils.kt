package com.example.sololeveling.ui.common

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AnimUtils {

    fun pulse(view: View, scale: Float = 1.05f, duration: Long = 1000, repeatCount: Int = ObjectAnimator.INFINITE) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, scale, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, scale, 1f)
        
        ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY).apply {
            this.duration = duration
            this.repeatCount = repeatCount
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    fun typewriter(
        textView: TextView, 
        text: String, 
        delayMs: Long = 30, 
        onComplete: (() -> Unit)? = null
    ) {
        val stringBuilder = StringBuilder()
        var index = 0
        
        val runnable = object : Runnable {
            override fun run() {
                if (index < text.length) {
                    stringBuilder.append(text[index])
                    textView.text = stringBuilder.toString()
                    index++
                    textView.postDelayed(this, delayMs)
                } else {
                    onComplete?.invoke()
                }
            }
        }
        
        textView.removeCallbacks(runnable)
        textView.text = "" // Reset
        textView.post(runnable)
    }

    fun shake(view: View) {
        val rotate = PropertyValuesHolder.ofFloat(View.ROTATION, 0f, -2f, 2f, -2f, 2f, 0f)
        ObjectAnimator.ofPropertyValuesHolder(view, rotate).apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
    
    fun fadeIn(view: View, duration: Long = 500) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setListener(null)
    }
}
