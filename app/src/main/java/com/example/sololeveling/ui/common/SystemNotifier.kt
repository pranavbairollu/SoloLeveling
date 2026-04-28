package com.example.sololeveling.ui.common

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.contains

object SystemNotifier {

    fun show(activity: Activity, message: String, type: SystemNotificationView.Type = SystemNotificationView.Type.INFO) {
        val decorView = activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)
        
        var notificationView: SystemNotificationView? = null
        
        // Check if already injected
        for (i in 0 until decorView.childCount) {
            val child = decorView.getChildAt(i)
            if (child is SystemNotificationView) {
                notificationView = child
                break
            }
        }
        
        // Inject if missing
        if (notificationView == null) {
            notificationView = SystemNotificationView(activity)
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            notificationView.layoutParams = params
            decorView.addView(notificationView)
            
            // Bring to front to ensure overlay
            notificationView.bringToFront()
        }
        
        // Show Message
        notificationView.show(message, type)
    }
}
