package id.xms.xtrakernelmanager.ui.screens.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.xms.xtrakernelmanager.domain.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * SetupViewModel separating root verification and setup state from the UI.
 * Automatically retries pending checks if an ON_RESUME event fires while a root request is blocking.
 */
class SetupViewModel : ViewModel() {

    private val _hasRoot = MutableStateFlow(false)
    val hasRoot: StateFlow<Boolean> = _hasRoot.asStateFlow()

    private val _isCheckingRoot = MutableStateFlow(false)
    val isCheckingRoot: StateFlow<Boolean> = _isCheckingRoot.asStateFlow()

    private val _pendingRootRetry = AtomicBoolean(false)

    init {
        // Initial passive non-prompting check on startup
        checkRootPassive()
    }

    /**
     * Active root check. Closes any cached non-root shell and triggers fresh SU prompt if needed.
     * Guards against concurrent runs and retries automatically if a request arrives while blocked.
     */
    fun checkRoot() {
        if (_isCheckingRoot.value) {
            _pendingRootRetry.set(true)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _isCheckingRoot.value = true
            try {
                val root = RootManager.isRootAvailable()
                _hasRoot.value = root
            } finally {
                _isCheckingRoot.value = false
                if (_pendingRootRetry.getAndSet(false) && !_hasRoot.value) {
                    checkRoot()
                }
            }
        }
    }

    /**
     * Passive check — inspects existing cached shell without showing prompts.
     */
    fun checkRootPassive() {
        viewModelScope.launch(Dispatchers.IO) {
            val root = RootManager.hasCachedRoot()
            if (root && !_hasRoot.value) {
                _hasRoot.value = true
            }
        }
    }
}
