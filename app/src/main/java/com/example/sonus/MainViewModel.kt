package com.example.sonus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _isBottomNavVisible = MutableLiveData<Boolean>(true)
    val isBottomNavVisible: LiveData<Boolean> = _isBottomNavVisible

    private val _isMiniPlayerVisible = MutableLiveData<Boolean>(false)
    val isMiniPlayerVisible: LiveData<Boolean> = _isMiniPlayerVisible

    // Tab Navigation State (0: DIR, 1: SRCH, 2: LIB, 3: SYS)
    private val _currentTabPage = MutableLiveData<Int>(0)
    val currentTabPage: LiveData<Int> = _currentTabPage

    // Library Filter State (0: ALL, 1: PLAYLISTS, 2: ALBUMS)
    private val _libraryFilter = MutableLiveData<Int>(0)
    val libraryFilter: LiveData<Int> = _libraryFilter

    // Persistent Terminal Logs
    private val _terminalLogs = MutableLiveData<List<String>>(listOf(
        "INITIALIZING_SYSTEM_CORE...",
        "BOOT_SEQUENCE_COMPLETE",
        "AUTHENTICATING_OPERATOR_V1.0",
        "CONNECTING_TO_REMOTE_SERVER...",
        "SCANNING_ARCHIVE_MODULES...",
        "SYSTEM_STATUS: OPTIMAL",
        "WAITING_FOR_SIGNAL..."
    ))
    val terminalLogs: LiveData<List<String>> = _terminalLogs

    fun setBottomNavVisibility(visible: Boolean) {
        _isBottomNavVisible.value = visible
    }

    fun setMiniPlayerVisibility(visible: Boolean) {
        _isMiniPlayerVisible.value = visible
    }

    fun setTabPage(page: Int) {
        if (_currentTabPage.value != page) {
            _currentTabPage.value = page
        }
    }

    fun setLibraryFilter(filter: Int) {
        _libraryFilter.value = filter
    }

    fun addTerminalLog(message: String) {
        val current = _terminalLogs.value?.toMutableList() ?: mutableListOf()
        current.add(message.uppercase())
        if (current.size > 20) current.removeAt(0)
        _terminalLogs.value = current
    }
}
