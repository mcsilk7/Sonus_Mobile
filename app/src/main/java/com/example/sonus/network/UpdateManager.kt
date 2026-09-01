package com.example.sonus.network

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.sonus.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

object UpdateManager {
    private const val TAG = "SonusUpdate"
    private var downloadId: Long = -1
    private val updateScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _downloadProgress = MutableStateFlow<Int>(-1)
    val downloadProgress: StateFlow<Int> = _downloadProgress

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
        _downloadProgress.value = 0
        val fileName = "SonusUpdate.apk"
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Sonus System Update v$version")
            .setDescription("Downloading technical upgrade...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = dm.enqueue(request)
        
        startProgressPolling(context, downloadId)
        
        // Register receiver for when download is done
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                Log.d(TAG, "Download complete received for ID: $id (expected: $downloadId)")
                if (id == downloadId) {
                    _downloadProgress.value = 100
                    installApk(ctx, id)
                    ctx.unregisterReceiver(this)
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun startProgressPolling(context: Context, id: Long) {
        updateScope.launch {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query().setFilterById(id)
                val cursor = dm.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (statusIndex != -1) {
                        val status = cursor.getInt(statusIndex)
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            _downloadProgress.value = 100
                            downloading = false
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            _downloadProgress.value = -1
                            downloading = false
                        } else {
                            val downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            val totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            if (downloadedIndex != -1 && totalIndex != -1) {
                                val downloaded = cursor.getLong(downloadedIndex)
                                val total = cursor.getLong(totalIndex)
                                if (total > 0) {
                                    val progress = ((downloaded * 100L) / total).toInt()
                                    _downloadProgress.value = progress
                                }
                            }
                        }
                    }
                }
                cursor?.close()
                delay(500) // Poll every 500ms
            }
        }
    }

    private fun installApk(context: Context, id: Long) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = dm.getUriForDownloadedFile(id)
        
        Log.d(TAG, "Installing APK from URI: $uri")

        if (uri != null) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start install activity", e)
            }
        } else {
            Log.e(TAG, "Download URI is null, cannot install.")
        }
    }
}
