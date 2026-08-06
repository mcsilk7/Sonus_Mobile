package com.example.sonus

import android.app.Activity
import android.content.Intent
import android.view.View

object NavigationHelper {

    fun setupBottomNav(activity: Activity) {

        // Home
        activity.findViewById<View>(R.id.navHome)?.setOnClickListener {
            if (activity !is MainActivity) {
                val intent = Intent(activity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            }
        }

        // Szukaj
        activity.findViewById<View>(R.id.navSearch)?.setOnClickListener {
            if (activity !is SearchActivity) {
                val intent = Intent(activity, SearchActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            }
        }

        // Biblioteka
        activity.findViewById<View>(R.id.navLibrary)?.setOnClickListener {
            if (activity !is LibraryActivity) {
                val intent = Intent(activity, LibraryActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            }
        }

        // Ustawienia
        activity.findViewById<View>(R.id.navSettings)?.setOnClickListener {
            if (activity !is SettingsActivity) {
                val intent = Intent(activity, SettingsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                activity.startActivity(intent)
            }
        }

        // Mini player -> Player
        activity.findViewById<View>(R.id.miniPlayer)?.setOnClickListener {
            val intent = Intent(activity, PlayerActivity::class.java)
            activity.startActivity(intent)
        }

        // Awatar -> Profil
        activity.findViewById<View>(R.id.userAvatar)?.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            activity.startActivity(intent)
        }
    }
}