package id.xms.xtrakernelmanager.ui.screens.info

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.ui.splash.UpdateConfig
import id.xms.xtrakernelmanager.ui.splash.UpdatePrefs
import id.xms.xtrakernelmanager.ui.splash.fetchUpdateConfig
import id.xms.xtrakernelmanager.ui.splash.isInternetAvailable
import id.xms.xtrakernelmanager.ui.splash.isUpdateAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class UpdateState(
    val isChecking: Boolean = false,
    val hasUpdate: Boolean = false,
    val updateConfig: UpdateConfig? = null,
    val error: String? = null,
    // Download
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,       // 0–100
    val downloadError: String? = null,
    val isInstallReady: Boolean = false   // APK ready, installer launched
)

class UpdateViewModel : ViewModel() {
    private val _updateState = MutableStateFlow(UpdateState())
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun checkForUpdates(context: Context) {
        viewModelScope.launch {
            _updateState.value = UpdateState(isChecking = true)

            try {
                val pendingUpdate = UpdatePrefs.getPendingUpdate(context)
                if (pendingUpdate != null && isUpdateAvailable(BuildConfig.VERSION_NAME, pendingUpdate.version)) {
                    if (isInternetAvailable(context)) {
                        _updateState.value = UpdateState(
                            isChecking = false,
                            hasUpdate = true,
                            updateConfig = pendingUpdate
                        )
                        val freshConfig = withTimeoutOrNull(3000L) { fetchUpdateConfig() }
                        if (freshConfig != null && isUpdateAvailable(BuildConfig.VERSION_NAME, freshConfig.version)) {
                            UpdatePrefs.savePendingUpdate(context, freshConfig.version, freshConfig.url, freshConfig.changelog)
                            _updateState.value = UpdateState(
                                isChecking = false,
                                hasUpdate = true,
                                updateConfig = freshConfig
                            )
                        }
                    } else {
                        _updateState.value = UpdateState(
                            isChecking = false,
                            hasUpdate = false,
                            error = "No internet connection"
                        )
                    }
                } else {
                    if (pendingUpdate != null) UpdatePrefs.clear(context)

                    if (isInternetAvailable(context)) {
                        val config = withTimeoutOrNull(5000L) { fetchUpdateConfig() }

                        if (config != null && isUpdateAvailable(BuildConfig.VERSION_NAME, config.version)) {
                            UpdatePrefs.savePendingUpdate(context, config.version, config.url, config.changelog)
                            _updateState.value = UpdateState(
                                isChecking = false,
                                hasUpdate = true,
                                updateConfig = config
                            )
                        } else {
                            _updateState.value = UpdateState(isChecking = false, hasUpdate = false)
                        }
                    } else {
                        _updateState.value = UpdateState(
                            isChecking = false,
                            hasUpdate = false,
                            error = "No internet connection"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateViewModel", "Error checking for updates", e)
                _updateState.value = UpdateState(
                    isChecking = false,
                    hasUpdate = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Download the APK from [url] into internal files dir, then launch the package installer.
     * Progress (0–100) is reported via [UpdateState.downloadProgress].
     */
    fun downloadAndInstall(context: Context, url: String) {
        viewModelScope.launch {
            val current = _updateState.value
            _updateState.value = current.copy(
                isDownloading = true,
                downloadProgress = 0,
                downloadError = null,
                isInstallReady = false
            )

            try {
                val apkFile = withContext(Dispatchers.IO) {
                    downloadApk(context, url) { progress ->
                        _updateState.value = _updateState.value.copy(downloadProgress = progress)
                    }
                }

                // Launch installer
                val apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(installIntent)

                _updateState.value = _updateState.value.copy(
                    isDownloading = false,
                    downloadProgress = 100,
                    isInstallReady = true
                )
            } catch (e: Exception) {
                Log.e("UpdateViewModel", "Download/install failed", e)
                _updateState.value = _updateState.value.copy(
                    isDownloading = false,
                    downloadError = e.message ?: "Download failed"
                )
            }
        }
    }

    private fun downloadApk(
        context: Context,
        urlString: String,
        onProgress: (Int) -> Unit
    ): File {
        val apkFile = File(context.filesDir, "xkm_update.apk")
        if (apkFile.exists()) apkFile.delete()

        var connection: HttpURLConnection? = null
        try {
            var currentUrl = urlString
            var redirectCount = 0

            // Follow redirects manually (GitHub releases redirect to CDN)
            while (redirectCount < 10) {
                val conn = URL(currentUrl).openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                conn.connectTimeout = 15_000
                conn.readTimeout = 30_000
                conn.setRequestProperty("User-Agent", "XKM-Updater/${BuildConfig.VERSION_NAME}")
                conn.connect()

                val responseCode = conn.responseCode
                if (responseCode in 300..399) {
                    val location = conn.getHeaderField("Location") ?: break
                    conn.disconnect()
                    currentUrl = location
                    redirectCount++
                    continue
                }
                connection = conn
                break
            }

            val conn = connection ?: throw Exception("Too many redirects")
            val contentLength = conn.contentLength.toLong()

            conn.inputStream.use { input ->
                apkFile.outputStream().use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var downloaded = 0L
                    var bytes: Int

                    while (input.read(buffer).also { bytes = it } != -1) {
                        output.write(buffer, 0, bytes)
                        downloaded += bytes
                        if (contentLength > 0) {
                            onProgress(((downloaded * 100) / contentLength).toInt())
                        }
                    }
                }
            }
        } finally {
            connection?.disconnect()
        }

        return apkFile
    }

    fun clearUpdate(context: Context) {
        UpdatePrefs.clear(context)
        _updateState.value = UpdateState(isChecking = false, hasUpdate = false)
    }
}
