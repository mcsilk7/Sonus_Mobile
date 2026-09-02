package ui

import com.example.sonus.DependencyContainer
import com.example.sonus.DesktopDateFormatter
import com.example.sonus.DesktopNetworkMonitor
import com.example.sonus.db.getDatabaseBuilder
import data.DesktopSessionManager

object DesktopDI {
    lateinit var container: DependencyContainer
    val sessionManager = DesktopSessionManager()

    fun init() {
        val database = getDatabaseBuilder().build()
        container = DependencyContainer(
            dao = database.musicDao(),
            networkMonitor = DesktopNetworkMonitor(),
            dateFormatter = DesktopDateFormatter(),
            getToken = { sessionManager.getToken() }
        )
    }
}
