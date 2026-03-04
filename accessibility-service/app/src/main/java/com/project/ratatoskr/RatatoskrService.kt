package com.project.ratatoskr


import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
class RatatoskrService: AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventType = AccessibilityEvent.eventTypeToString(event?.eventType ?: 0 )
        Log.d("RATATOSKR", "Event Type $eventType")
        val source: AccessibilityNodeInfo? = event?.source
        if(source != null){
            val viewId = source.viewIdResourceName ?: "no-id"
            val sourceText = source.text ?: "no-text"
            val clsName = source.className ?: "unknown-class"

            Log.d("RATATOSKR", "Element: $clsName ID: $viewId Text: $sourceText" )

            //for API Level < 34
            source.recycle()
        }

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("RATATOSKR", "Service is Running")
    }

    override fun onInterrupt() {
        Log.i("RATATOSKR","Service stopped")
    }
}