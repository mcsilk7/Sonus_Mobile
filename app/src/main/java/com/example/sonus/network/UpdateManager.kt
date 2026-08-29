package com.example.sonus.network

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.sonus.BuildConfig
import java.io.File

object UpdateManager {
    private const val TAG = "SonusUpdate"
    private var downloadId: Long = -1

    fun checkAndDownloadUpdate(context: Context, latestRelease: GithubRelease) {
        val currentVersion = BuildConfig.VERSION_NAME
        val newVersion = latestRelease.tagName.removePrefix("v")
        
        if (isNewerVersion(currentVersion, newVersion)) {
            val apkAsset = latestRelease.assets.find { it.name.endsWith(".apk") }
            if (apkAsset != null) {
                startDownload(context, apkAsset.downloadUrl, newVersion)
            }
        }
    }

    private fun isNewerVersion(current: String, new: String): Boolean {
        return try {
            val currParts = current.split(".").map { it.toInt() }
            val newParts = new.split(".").map { it.toInt() }
            for (i in 0 until minOf(currParts.size, newParts.size)) {
                if (newParts[i] > currParts[i]) return true
                if (newParts[i] < currParts[i]) return false
            }
            newParts.size > currParts.size
        } catch (e: Exception) {
            new > current
        }
    }

    private fun startDownload(context: Context, url: String, version: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Sonus System Update v$version")
            .setDescription("Downloading technical upgrade...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "sonus_update_$version.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = dm.enqueue(request)
        
        // Register receiver for when download is done
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installApk(ctx, version)
                    ctx.unregisterReceiver(this)
                }
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    private fun installApk(context: Context, version: String) {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "sonus_update_$version.apk")
        // Note: For public directory we need a different approach, let's use the safer one
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val apkFile = File(downloadsDir, "sonus_update_$version.apk")

        if (apkFile.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        }
    }
}
