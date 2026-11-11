package id.xms.xtrakernelmanager.ui.splash

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import id.xms.xtrakernelmanager.BuildConfig
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import kotlin.math.max

// --- DATA CLASSES ---
data class UpdateConfig(
    val version: String = "",
    val changelog: List<String> = emptyList(),
    val url: String = "",
    val force: Boolean = false,
    val channel: String = "release" // "release" or "beta"
)

object UpdatePrefs {
    private const val PREF_NAME = "update_prefs"
    private const val KEY_PENDING_VERSION = "pending_version"
    private const val KEY_UPDATE_URL = "update_url"
    private const val KEY_CHANGELOG = "update_changelog"

    fun savePendingUpdate(context: Context, version: String, url: String, changelog: List<String>) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putString(KEY_PENDING_VERSION, version)
            putString(KEY_UPDATE_URL, url)
            putString(KEY_CHANGELOG, changelog.joinToString("\n"))
            apply()
        }
    }

    fun getPendingUpdate(context: Context): UpdateConfig? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val version = prefs.getString(KEY_PENDING_VERSION, null) ?: return null
        val url = prefs.getString(KEY_UPDATE_URL, "") ?: ""
        val changelogStr = prefs.getString(KEY_CHANGELOG, "") ?: ""
        // Split changelog back to list
        val changelog = if (changelogStr.isNotEmpty()) {
            changelogStr.split("\n").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
        return UpdateConfig(version, changelog, url, force = true) // Diasumsikan force jika tersimpan
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

// --- HELPER FUNCTIONS ---

/**
 * Check if the device has root access using RootManager (libsu)
 */
suspend fun checkRootAccess(): Boolean = id.xms.xtrakernelmanager.domain.root.RootManager.isRootAvailable()

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

suspend fun fetchUpdateConfig(): UpdateConfig? = withContext(Dispatchers.IO) {
    fetchFromGitHubReleases(isBeta = false)
}

suspend fun fetchBetaUpdateConfig(): UpdateConfig? = withContext(Dispatchers.IO) {
    fetchFromGitHubReleases(isBeta = true)
}

private fun fetchFromGitHubReleases(isBeta: Boolean): UpdateConfig? {
    return try {
        Log.d("OTAUpdate", "Fetching update config from GitHub Releases (isBeta=$isBeta)...")
        val url = URL("https://api.github.com/repos/Xtra-Manager-Software/Xtra-Kernel-Manager/releases")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5000
            readTimeout = 5000
            setRequestProperty("Accept", "application/vnd.github.v3+json")
            setRequestProperty("User-Agent", "XKM-App")
        }

        if (conn.responseCode != 200) {
            Log.e("OTAUpdate", "GitHub releases API returned HTTP ${conn.responseCode}")
            return null
        }

        val jsonString = conn.inputStream.bufferedReader().use { it.readText() }
        val array = JSONArray(jsonString)

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val isPrerelease = item.optBoolean("prerelease", false)
            val tagName = item.optString("tag_name", "")
            val isBetaRelease = isPrerelease || tagName.contains("beta", ignoreCase = true) || tagName.contains("rc", ignoreCase = true)

            if (isBeta == isBetaRelease) {
                val body = item.optString("body", "")
                val changelog = body.replace("\r", "")
                    .split("\n")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                var downloadUrl = item.optString("html_url", "")
                val assets = item.optJSONArray("assets")
                if (assets != null) {
                    for (j in 0 until assets.length()) {
                        val asset = assets.getJSONObject(j)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = asset.optString("browser_download_url", downloadUrl)
                            break
                        }
                    }
                }

                val version = when {
                    tagName.startsWith("release-stable-") -> tagName.removePrefix("release-stable-") + "-Release"
                    tagName.startsWith("release-beta1-") -> tagName.removePrefix("release-beta1-") + "-Beta1"
                    tagName.startsWith("release-") -> tagName.removePrefix("release-")
                    else -> tagName
                }

                Log.d("OTAUpdate", "Found update from GitHub: version=$version, url=$downloadUrl, channel=${if (isBeta) "beta" else "release"}")
                return UpdateConfig(
                    version = version,
                    changelog = changelog,
                    url = downloadUrl,
                    force = false,
                    channel = if (isBeta) "beta" else "release"
                )
            }
        }
        null
    } catch (e: Exception) {
        Log.e("OTAUpdate", "Error fetching from GitHub releases: ${e.message}", e)
        null
    }
}

fun isUpdateAvailable(currentVersion: String, remoteVersion: String): Boolean {
    return try {
        Log.d("OTA", "Comparing versions: current=$currentVersion, remote=$remoteVersion")

        // Parse base version (sebelum tanda -)
        val currentBase = currentVersion.substringBefore("-").trim()
        val remoteBase = remoteVersion.substringBefore("-").trim()

        // Parse suffix (setelah tanda -)
        val currentSuffix = if (currentVersion.contains("-")) currentVersion.substringAfter("-").trim() else ""
        val remoteSuffix = if (remoteVersion.contains("-")) remoteVersion.substringAfter("-").trim() else ""

        // Bersihkan dan bandingkan base version (2.0, 2.1, dll)
        val cleanCurrent = currentBase.replace(Regex("[^0-9.]"), "")
        val cleanRemote = remoteBase.replace(Regex("[^0-9.]"), "")
        val cParts = cleanCurrent.split(".").map { it.toIntOrNull() ?: 0 }
        val rParts = cleanRemote.split(".").map { it.toIntOrNull() ?: 0 }
        val length = max(cParts.size, rParts.size)

        // Bandingkan base version
        for (i in 0 until length) {
            val c = cParts.getOrElse(i) { 0 }
            val r = rParts.getOrElse(i) { 0 }
            if (r > c) {
                Log.d("OTA", "Remote base version is higher: $r > $c")
                return true // Remote lebih tinggi (2.1 > 2.0)
            }
            if (r < c) {
                Log.d("OTA", "Current base version is higher: $c > $r")
                return false // Current lebih tinggi (2.1 > 2.0)
            }
        }

        // Jika base version sama, bandingkan suffix
        Log.d("OTA", "Base versions equal, comparing suffixes: current='$currentSuffix', remote='$remoteSuffix'")

        // Get priorities for both suffixes
        val currentPriority = getSuffixPriority(currentSuffix)
        val remotePriority = getSuffixPriority(remoteSuffix)

        Log.d("OTA", "Suffix priorities: current=$currentPriority, remote=$remotePriority")

        // Hanya update available jika remote priority lebih tinggi
        val result = remotePriority > currentPriority
        Log.d("OTA", "Update available: $result")
        result
    } catch (e: Exception) {
        Log.e("OTA", "Error comparing versions: $currentVersion vs $remoteVersion", e)
        false
    }
}


fun getSuffixPriority(suffix: String): Int {
    if (suffix.isEmpty()) return 50 // Versi tanpa suffix (2.0) = stable release

    val lowerSuffix = suffix.lowercase()

    return when {
        lowerSuffix.startsWith("release") -> 100
        lowerSuffix.startsWith("stable") -> 90 
        lowerSuffix.startsWith("rc") -> {
            val num = Regex("[0-9]+").find(suffix)?.value?.toIntOrNull() ?: 0
            40 + num
        }
        lowerSuffix.startsWith("beta") -> {
            val num = Regex("[0-9]+").find(suffix)?.value?.toIntOrNull() ?: 0
            20 + num
        }
        lowerSuffix.startsWith("alpha") -> {
            val num = Regex("[0-9]+").find(suffix)?.value?.toIntOrNull() ?: 0
            10 + num
        }
        else -> 0
    }
}