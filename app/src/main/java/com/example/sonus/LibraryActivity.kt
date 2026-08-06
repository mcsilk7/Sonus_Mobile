package com.example.sonus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LibraryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        NavigationHelper.setupBottomNav(this)
    }
}