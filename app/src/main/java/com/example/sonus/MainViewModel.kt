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
}
