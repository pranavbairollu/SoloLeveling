package com.example.sololeveling.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class ParticleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private data class Particle(
        var x: Float,
        var y: Float,
        var dx: Float,
        var dy: Float,
        var radius: Float,
        var alpha: Int
    )

    private val particles = mutableListOf<Particle>()
    private val paint = Paint().apply {
        color = Color.parseColor("#00E5FF") // System Blue
        style = Paint.Style.FILL
    }
    private val random = Random(System.currentTimeMillis())
    private var isInitialized = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!isInitialized && w > 0 && h > 0) {
            initParticles(w, h)
            isInitialized = true
        }
    }

    private fun initParticles(width: Int, height: Int) {
        particles.clear()
        for (i in 0 until 50) { // 50 particles
            particles.add(
                Particle(
                    x = random.nextFloat() * width,
                    y = random.nextFloat() * height,
                    dx = (random.nextFloat() - 0.5f) * 0.5f,
                    dy = (random.nextFloat() - 0.5f) * 0.5f, // Slow drift
                    radius = random.nextFloat() * 4f + 1f,
                    alpha = random.nextInt(50, 200)
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        particles.forEach { p ->
            paint.alpha = p.alpha
            canvas.drawCircle(p.x, p.y, p.radius, paint)

            // Update position
            p.x += p.dx
            p.y += p.dy

            // Wrap around screen
            if (p.x < 0) p.x = width.toFloat()
            if (p.x > width) p.x = 0f
            if (p.y < 0) p.y = height.toFloat()
            if (p.y > height) p.y = 0f
        }

        invalidate() // Infinite loop
    }
}
