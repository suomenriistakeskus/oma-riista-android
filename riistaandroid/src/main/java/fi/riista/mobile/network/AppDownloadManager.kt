package fi.riista.mobile.network

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import fi.riista.common.RiistaSDK
import fi.riista.mobile.AppConfig
import fi.riista.mobile.R
import java.net.URL
import java.util.*

class AppDownloadManager(val context: Context) {

    private val downloads = Collections.synchronizedMap(HashMap<Long, String>())
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    init {
        context.registerReceiver(
            downloadCompleteReceiver(downloadManager),
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
        )
    }

    fun startDownload(url: URL, fileName: String) {
        val host = URL(AppConfig.getBaseAddress()).host
        val request = DownloadManager.Request(Uri.parse(url.toString()))
            .setTitle(context.getString(R.string.download_manager_download_title, fileName))
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                fileName,
            )
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .addRequestHeader("Cookie", getCookies(host))
        val downloadId = downloadManager.enqueue(request)
        downloads[downloadId] = fileName
        Toast.makeText(context, context.getString(R.string.download_manager_downloading_file), Toast.LENGTH_SHORT).show()
    }

    private fun downloadCompleteReceiver(downloadManager: DownloadManager): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val query = DownloadManager.Query().setFilterById(*downloads.keys.toLongArray())
                val cursor = downloadManager.query(query)
                if (cursor != null) {
                    cursor.moveToFirst()
                    val idColumn = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
                    val downloadId = cursor.getLong(idColumn)
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(statusIndex)
                    val fileName = downloads[downloadId]
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        downloads.remove(downloadId)
                        fileName?.let {
                            Toast.makeText(
                                context,
                                context.getString(R.string.download_manager_download_complete, it),
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        downloads.remove(downloadId)
                        fileName?.let {
                            Toast.makeText(
                                context,
                                context.getString(R.string.download_manager_download_failed, it),
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun getCookies(host: String): String {
        return RiistaSDK.getAllNetworkCookies()
            .filter { cookie -> cookie.domain == host }
            .joinToString(separator = "; ") { cookie ->
                "${cookie.name}=${cookie.value}"
            }
    }
}
