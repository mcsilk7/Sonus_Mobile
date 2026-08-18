package com.example.sonus

import android.content.Context

object LabelProvider {

    fun getLabel(context: Context, key: String): String {
        val settingsManager = SettingsManager(context)
        val isTechnical = settingsManager.getThemeId() == 0 // Only Amber (0) is technical

        return when (key) {
            // Navigation
            "nav_home" -> context.getString(if (isTechnical) R.string.nav_dir else R.string.nav_home_norm)
            "nav_search" -> context.getString(if (isTechnical) R.string.nav_srch else R.string.nav_search_norm)
            "nav_library" -> context.getString(if (isTechnical) R.string.nav_lib else R.string.nav_library_norm)
            "nav_settings" -> context.getString(if (isTechnical) R.string.nav_sys else R.string.nav_settings_norm)
            "nav_back" -> context.getString(if (isTechnical) R.string.nav_esc else R.string.nav_back_norm)
            
            // Home (DIR)
            "home_header_top" -> context.getString(if (isTechnical) R.string.station_control_unit else R.string.welcome_norm)
            "home_header_main" -> context.getString(if (isTechnical) R.string.main_console else R.string.dashboard_norm)
            "home_fav_card" -> context.getString(if (isTechnical) R.string.fav_data else R.string.favorites_norm)
            "home_fav_desc" -> context.getString(if (isTechnical) R.string.unit_01 else R.string.favorites_desc_norm)
            "home_pl_card" -> context.getString(if (isTechnical) R.string.pl_data else R.string.playlists_norm)
            "home_pl_desc" -> context.getString(if (isTechnical) R.string.unit_02 else R.string.playlists_desc_norm)
            "home_access" -> context.getString(if (isTechnical) R.string.access_bracket else R.string.access_norm)
            "home_recent_header" -> context.getString(if (isTechnical) R.string.loaded_reels_archive else R.string.recent_norm)
            "home_log_header" -> context.getString(if (isTechnical) R.string.system_status_log else R.string.status_log_norm)
            "home_live_feed" -> context.getString(if (isTechnical) R.string.live_feed else R.string.live_feed_norm)
            
            // Search (SRCH)
            "search_header_top" -> context.getString(if (isTechnical) R.string.signal_scanner else R.string.search_title_norm)
            "search_header_main" -> context.getString(if (isTechnical) R.string.track_finder_unit else R.string.search_desc_norm)
            "search_hint" -> context.getString(if (isTechnical) R.string.search_hint else R.string.search_hint_norm)
            "search_history" -> context.getString(if (isTechnical) R.string.recently_searched else R.string.history_norm)
            "search_results_songs" -> context.getString(if (isTechnical) R.string.search_results_songs else R.string.results_songs_norm)
            "search_results_albums" -> context.getString(if (isTechnical) R.string.search_results_albums else R.string.results_albums_norm)

            // Library (LIB)
            "library_header_top" -> context.getString(if (isTechnical) R.string.archive_unit else R.string.library_norm)
            "library_header_main" -> context.getString(if (isTechnical) R.string.library_management else R.string.library_desc_norm)
            "library_fav_signals" -> context.getString(if (isTechnical) R.string.favorite_signals else R.string.favorites_full_norm)
            "library_open_archive" -> context.getString(if (isTechnical) R.string.open_archive_storage else R.string.storage_norm)
            "library_open" -> context.getString(if (isTechnical) R.string.double_colon_open else R.string.open_norm)
            "library_tab_all" -> context.getString(if (isTechnical) R.string.tab_all else R.string.all_norm)
            "library_tab_playlists" -> context.getString(if (isTechnical) R.string.tab_playlists else R.string.playlists_label_norm)
            "library_tab_albums" -> context.getString(if (isTechnical) R.string.tab_albums else R.string.albums_label_norm)
            "library_sec_playlists" -> context.getString(if (isTechnical) R.string.storage_playlists else R.string.playlists_norm)
            "library_sec_albums" -> context.getString(if (isTechnical) R.string.storage_albums else R.string.albums_label_norm)
            "library_init_playlist" -> context.getString(if (isTechnical) R.string.initialize_playlist else R.string.new_playlist_norm)
            "library_create_disk" -> context.getString(if (isTechnical) R.string.create_new_virtual_disk else R.string.new_disk_norm)
            "library_exe" -> context.getString(if (isTechnical) R.string.exe_bracket else R.string.execute_norm)
            "library_load" -> context.getString(if (isTechnical) R.string.double_colon_load else R.string.load_norm)
            "library_id_label" -> context.getString(if (isTechnical) R.string.unit_id_prefix else R.string.id_prefix_norm)
            "library_log_label" -> context.getString(if (isTechnical) R.string.log_prefix else R.string.status_prefix_norm)
            
            // Player
            "player_header" -> context.getString(if (isTechnical) R.string.station_monitor else R.string.player_norm)
            "player_shf" -> context.getString(if (isTechnical) R.string.shf else R.string.shuffle_norm)
            "player_rpt" -> context.getString(if (isTechnical) R.string.rpt else R.string.repeat_norm)
            "player_queue" -> context.getString(if (isTechnical) R.string.view_queue else R.string.queue_norm)
            "queue_title" -> context.getString(if (isTechnical) R.string.station_queue else R.string.queue_norm)
            "queue_desc" -> context.getString(if (isTechnical) R.string.drag_to_reorder else R.string.reorder_norm)

            // Details
            "album_detail_top" -> context.getString(if (isTechnical) R.string.album_access_point else R.string.album_detail_norm)
            "album_detail_main" -> context.getString(if (isTechnical) R.string.disk_archive_unit else R.string.library_norm)
            "playlist_detail_top" -> context.getString(if (isTechnical) R.string.storage_access_module else R.string.playlist_detail_norm)
            "playlist_detail_main" -> context.getString(if (isTechnical) R.string.virtual_disk_management else R.string.library_norm)
            "data_stream_list" -> context.getString(if (isTechnical) R.string.data_stream_list else R.string.tracklist_norm)

            // Settings (SYS)
            "settings_header_top" -> context.getString(if (isTechnical) R.string.system_config else R.string.settings_norm)
            "settings_header_main" -> context.getString(if (isTechnical) R.string.station_settings else R.string.config_norm)
            "settings_op_name" -> context.getString(if (isTechnical) R.string.operator_name else R.string.name_label_norm)
            "settings_id_label" -> context.getString(if (isTechnical) R.string.id_label else R.string.mail_label_norm)
            "settings_edit" -> context.getString(if (isTechnical) R.string.double_colon_edit else R.string.edit_norm)
            "settings_visuals" -> context.getString(if (isTechnical) R.string.visual_configuration else R.string.visuals_norm)
            "settings_theme_engine" -> context.getString(if (isTechnical) R.string.theme_engine_selection else R.string.theme_label_norm)
            "settings_select" -> context.getString(if (isTechnical) R.string.double_colon_select else R.string.theme_select_norm)
            "settings_reels" -> context.getString(if (isTechnical) R.string.tape_reel_animation else R.string.reels_norm)
            "settings_reels_desc" -> context.getString(if (isTechnical) R.string.toggle_mechanical_visuals else R.string.reels_desc_norm)
            "settings_logout" -> context.getString(if (isTechnical) R.string.terminate_session else R.string.logout_norm)
            "settings_build" -> context.getString(if (isTechnical) R.string.build_info else R.string.build_norm)

            // Profile
            "profile_header_top" -> context.getString(if (isTechnical) R.string.operator_profile else R.string.profile_norm)
            "profile_header_main" -> context.getString(if (isTechnical) R.string.user_id_control else R.string.account_norm)
            "profile_edit_id" -> context.getString(if (isTechnical) R.string.edit_identity else R.string.edit_profile_norm)
            "profile_units" -> context.getString(if (isTechnical) R.string.units_label else R.string.stats_playlists_norm)
            "profile_runtime" -> context.getString(if (isTechnical) R.string.runtime_label else R.string.stats_hours_norm)
            "profile_signals" -> context.getString(if (isTechnical) R.string.signals_label else R.string.stats_favs_norm)
            "profile_acc_access" -> context.getString(if (isTechnical) R.string.account_access_bracket else R.string.danger_zone_norm)
            "profile_delete" -> context.getString(if (isTechnical) R.string.delete_account else R.string.delete_norm)
            "profile_wipe_desc" -> context.getString(if (isTechnical) R.string.wipe_all_user_data else R.string.delete_desc_norm)
            "profile_wipe" -> context.getString(if (isTechnical) R.string.double_colon_wipe else R.string.wipe_norm)
            "profile_terminate" -> context.getString(if (isTechnical) R.string.terminate_session else R.string.logout_norm)
            "profile_build" -> context.getString(if (isTechnical) R.string.build_ver else R.string.build_norm)

            "theme_amber" -> context.getString(if (isTechnical) R.string.theme_amber_os else R.string.theme_amber_os_norm)
            "theme_dark" -> context.getString(if (isTechnical) R.string.theme_ind_dark else R.string.theme_ind_dark_norm)
            "theme_light" -> context.getString(if (isTechnical) R.string.theme_ind_light else R.string.theme_ind_light_norm)
            
            else -> key
        }
    }
}
