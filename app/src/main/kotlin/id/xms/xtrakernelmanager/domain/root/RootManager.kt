package id.xms.xtrakernelmanager.domain.root

import android.util.Log
import com.topjohnwu.superuser.Shell
import id.xms.xtrakernelmanager.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Clean facade for root operations using libsu.
 * Correctly manages cached shell lifecycles to prevent getting stuck on non-root cached shells.
 */
object RootManager {

    private const val TAG = "RootManager"

    // Debug bypass devices (only active in DEBUG builds)
    private val DEBUG_BYPASS_DEVICES = setOf("I2219", "Android SDK built for x86_64", "sdk_gphone16k_x86_64")

    fun isDebugBypassDevice(): Boolean {
        return BuildConfig.DEBUG && android.os.Build.MODEL in DEBUG_BYPASS_DEVICES
    }

    /**
     * Active check: closes any stale non-root cached shell first, then requests fresh root shell.
     * Prevents the app from being stuck on an ungranted shell after the user grants SU in Magisk/KernelSU/APatch.
     */
    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        if (isDebugBypassDevice()) {
            Log.d(TAG, "Debug bypass active for device: ${android.os.Build.MODEL}")
            return@withContext true
        }

        val cached = Shell.getCachedShell()
        Log.d(TAG, "isRootAvailable() called | cached=$cached | cachedIsRoot=${cached?.isRoot}")

        // If there's a cached shell that isn't root, close it so libsu builds a fresh one.
        if (cached != null && !cached.isRoot) {
            Log.d(TAG, "isRootAvailable() closing non-root cached shell")
            try {
                cached.close()
            } catch (e: Throwable) {
                Log.e(TAG, "Error closing cached shell: ${e.message}")
            }
        }

        return@withContext try {
            var shell = Shell.getShell()
            if (!shell.isRoot) {
                try {
                    shell.close()
                } catch (_: Throwable) {}

                shell = Shell.Builder.create()
                    .setTimeout(10)
                    .build("su")
            }

            val result = shell.isRoot
            Log.d(TAG, "isRootAvailable() returned isRoot=$result")
            result
        } catch (e: Throwable) {
            Log.e(TAG, "isRootAvailable() exception: ${e.javaClass.simpleName}: ${e.message}")
            false
        }
    }

    /**
     * Passive check: only reads the already-open cached shell, never prompts.
     */
    fun hasCachedRoot(): Boolean {
        val cached = Shell.getCachedShell()
        val result = cached?.isRoot ?: false
        Log.d(TAG, "hasCachedRoot() cached=$cached isRoot=$result")
        return result
    }

    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Executing: $command")
            val result = Shell.cmd(command).exec()
            if (result.isSuccess) {
                val output = result.out.joinToString("\n")
                Result.success(output)
            } else {
                val error = result.err.joinToString("\n")
                Log.e(TAG, "Failed: $error")
                Result.failure(Exception("Command failed: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun readFile(path: String): Result<String> =
        executeCommand("cat $path 2>/dev/null")

    suspend fun writeFile(path: String, content: String): Result<Unit> =
        executeCommand("echo '$content' > $path").map { Unit }

    suspend fun fileExists(path: String): Boolean = withContext(Dispatchers.IO) {
        val result = Shell.cmd("[ -f $path ] && echo exists || [ -d $path ] && echo exists").exec()
        result.out.firstOrNull() == "exists"
    }

    suspend fun getProp(name: String): String = withContext(Dispatchers.IO) {
        executeCommand("getprop $name").getOrNull()?.trim() ?: ""
    }

    suspend fun setProp(name: String, value: String): Result<Unit> =
        executeCommand("setprop $name $value").map { Unit }
}
