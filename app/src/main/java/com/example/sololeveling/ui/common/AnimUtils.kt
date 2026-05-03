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
import kotlinx.coroutines.isActive
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
        // Cancel previous job on THIS view
        (textView.tag as? kotlinx.coroutines.Job)?.cancel()
        
        val job = CoroutineScope(Dispatchers.Main).launch {
            val stringBuilder = StringBuilder()
            textView.text = ""
            
            for (char in text) {
                if (!isActive) break
                stringBuilder.append(char)
                textView.text = stringBuilder.toString()
                delay(delayMs)
            }
            
            if (isActive) {
                onComplete?.invoke()
            }
        }
        textView.tag = job
    }

    fun shake(view: View, intensity: Float = 10f) {
        val translationX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f, -intensity, intensity, -intensity, intensity, 0f)
        val translationY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, intensity, -intensity, intensity, -intensity, 0f)
        
        ObjectAnimator.ofPropertyValuesHolder(view, translationX, translationY).apply {
            duration = 400
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }
    
    fun glitchEffect(view: View, duration: Long = 1000, onComplete: (() -> Unit)? = null) {
        val originalAlpha = view.alpha
        val originalScaleX = view.scaleX
        val originalScaleY = view.scaleY
        
        CoroutineScope(Dispatchers.Main).launch {
            val endTime = System.currentTimeMillis() + duration
            while (System.currentTimeMillis() < endTime) {
                view.alpha = if (kotlin.random.Random.nextBoolean()) 0.2f else 0.8f
                view.scaleX = originalScaleX + (kotlin.random.Random.nextFloat() - 0.5f) * 0.1f
                view.scaleY = originalScaleY + (kotlin.random.Random.nextFloat() - 0.5f) * 0.1f
                view.translationX = (kotlin.random.Random.nextFloat() - 0.5f) * 20f
                delay(50)
            }
            
            // Reset
            view.alpha = originalAlpha
            view.scaleX = originalScaleX
            view.scaleY = originalScaleY
            view.translationX = 0f
            onComplete?.invoke()
        }
    }
    
    fun glowPulse(view: View, startAlpha: Float = 0.1f, endAlpha: Float = 0.6f, duration: Long = 2000) {
        val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, startAlpha, endAlpha, startAlpha)
        alpha.duration = duration
        alpha.repeatCount = ObjectAnimator.INFINITE
        alpha.interpolator = AccelerateDecelerateInterpolator()
        alpha.start()
        view.tag = alpha // Store for cancellation
    }
}
