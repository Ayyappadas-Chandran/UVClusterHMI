package com.ultraviolette.uvclusterhmi

import android.R
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.BugreportManager
import android.os.BugreportParams
import android.os.ParcelFileDescriptor
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL


class BugReportHelper {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    /**
     * Starts the official AOSP bug report generation.
     */
    fun startBugReport(context: Context) {
        val bm = context.getSystemService(BugreportManager::class.java)
        val outZip = File(context.getExternalFilesDir(null), "scooter_report.zip")
        try {
            val pfd = ParcelFileDescriptor.open(
                outZip,
                ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_READ_WRITE
            )

            /*bm.startBugreport(
                pfd, null,
                BugreportParams(BugreportParams.BUGREPORT_MODE_FULL),
                context.getMainExecutor(),
                object : BugreportManager.BugreportCallback() {
                    override fun onProgress(progress: Float) { *//* Update UI Bar if needed *//*
                    }

                    override fun onError(errorCode: Int) { *//* Handle Error *//*
                    }

                    override fun onFinished() {
                        // Automatically show the UI overlay when the report is ready
                        showCrashOverlay(context, outZip)
                    }
                })*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Shows a System Overlay UI using WindowManager.
     * This appears on top of all other apps/activities.
     */
    fun showCrashOverlay(context: Context, reportFile: File) {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?


        // Layout Parameters for System Overlay
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,  // Required for system-wide display
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP
        params.y = 50 // Offset from top

        val textView = TextView(context)
        textView.text = "Bug Report Ready"
        textView.setBackgroundColor(Color.RED)
        textView.setTextColor(Color.WHITE)
        textView.textSize = 18f
        textView.setPadding(40, 40, 40, 40)
        textView.setOnClickListener {
            shareReport(context, reportFile)
            removeOverlay()
        }

        overlayView = textView

        windowManager?.addView(overlayView, params)
        // Inflate a custom view (Assuming you have a layout file named 'overlay_crash.xml')
        // For this example, we'll create a simple programmatic view if layout isn't found
        val inflater = LayoutInflater.from(context)
        overlayView = inflater.inflate(R.layout.simple_list_item_1, null)


        // Note: In production, use a custom XML with a Red background and "UPLOAD" button.

        // Setup a simple button in the overlay (conceptually)
        overlayView!!.setOnClickListener(View.OnClickListener { v: View? ->
            shareReport(context, reportFile)
            removeOverlay()
        })

        windowManager!!.addView(overlayView, params)
    }

    private fun removeOverlay() {
        if (windowManager != null && overlayView != null) {
            windowManager!!.removeView(overlayView)
            overlayView = null
        }
    }

    /**
     * Opens the System Share sheet (Gmail, OneDrive, Drive, etc.)
     */
    private fun shareReport(context: Context, file: File) {
        val contentUri = FileProvider.getUriForFile(
            context,
            context.getPackageName() + ".fileprovider", file
        )

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("application/zip")
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Scooter Diagnostic: " + file.getName())
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(Intent.createChooser(shareIntent, "Upload BugReport to..."))
    }

    /**
     * Logic for a silent background upload to a company server.
     */
    private fun uploadToCloud(file: File) {
        val serverUrl = "https://your-scooter-cloud.com/api/logs"
        try {
            val conn = URL(serverUrl).openConnection() as HttpURLConnection
            conn.setDoOutput(true)
            conn.setRequestMethod("POST")
            conn.setRequestProperty("Content-Type", "application/zip")

            conn.getOutputStream().use { out ->
                FileInputStream(file).use { `in` ->
                    val buffer = ByteArray(4096)
                    var read: Int
                    while ((`in`.read(buffer).also { read = it }) != -1) {
                        out.write(buffer, 0, read)
                    }
                }
            }
            val responseCode = conn.getResponseCode()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
