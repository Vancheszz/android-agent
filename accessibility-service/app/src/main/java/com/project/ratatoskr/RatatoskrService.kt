package com.project.ratatoskr


import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.Executors


class RatatoskrService: AccessibilityService() {

    private val executor = Executors.newSingleThreadExecutor()
    private var outputStream : OutputStream? = null
    private var socket: Socket ?= null
    private var isConnecting = false


    private fun connectToAgent(){
        if (isConnecting) return
        isConnecting = true

        executor.execute {
            //try to connnect
            try {
                socket = Socket("127.0.0.1", 9999)
                outputStream = socket?.getOutputStream()
                Log.d("RATATOSKR", "Connect to Agent Successfully")
            }catch (e: Exception){
                Log.e("RATATOSKR", "Fail Connection")
                Thread.sleep(5000)
                //recursive connection
                connectToAgent()
            }
        }
    }
    private fun captureScreen(rootNode: AccessibilityNodeInfo): ScreenDump{
        val dumpBuilder = ScreenDump.newBuilder()
            .setPackageName(rootNode.packageName?.toString() ?: "unknown")
            .setTimestamp(System.currentTimeMillis())
            // Здесь можно добавить реальные размеры экрана из WindowManager
            .setWidth(1080)
            .setHeight(1920)

        flattenTree(rootNode, dumpBuilder)
        return dumpBuilder.build()

    }
    private fun flattenTree(node: AccessibilityNodeInfo, dumpBuilder: ScreenDump.Builder) {
        // collect data if have ID
        val text = node.text?.toString() ?: ""
        val resId = node.viewIdResourceName ?: ""

        if (text.isNotEmpty() || resId.isNotEmpty() || node.isClickable) {
            val bounds = android.graphics.Rect()
            node.getBoundsInScreen(bounds)

            val nodeProto = UiNode.newBuilder()
                .setText(text)
                .setResourceId(resId)
                .setClassName(node.className?.toString() ?: "")
                .setIsClickable(node.isClickable)
                .setBounds(Rect.newBuilder()
                    .setLeft(bounds.left)
                    .setTop(bounds.top)
                    .setRight(bounds.right)
                    .setBottom(bounds.bottom))
                .build()

            dumpBuilder.addNodes(nodeProto)
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let {
                flattenTree(it, dumpBuilder)
                it.recycle()
            }
        }
    }
    private fun SendDump(dump: ScreenDump){
            try{
               outputStream?.let{ stream ->
                   val bytes = dump.toByteArray()
                   val size = bytes.size
                   stream.write(byteArrayOf(
                       (size shr 24 ).toByte(),
                       (size shr 16).toByte(),
                       (size shr 8).toByte(),
                       size.toByte()
                   ))
                   stream.write(bytes)
                   stream.flush()
               }
            }catch (e: Exception){
                Log.e("RATATOSKR", "Sending Error: ${e.message}")
                connectToAgent() // try to recconect
            }
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
       if(event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
           val rootNode = rootInActiveWindow ?: return

           executor.execute{
               val dump = captureScreen(rootNode)
               SendDump(dump)
               rootNode.recycle()
           }

       }

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("RATATOSKR", "Service is Running")
        connectToAgent()
    }

    override fun onInterrupt() {
        Log.i("RATATOSKR","Service stopped")
    }
}