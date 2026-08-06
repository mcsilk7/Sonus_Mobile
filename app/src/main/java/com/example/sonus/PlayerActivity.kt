package com.example.sonus

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class PlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Powrót
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}