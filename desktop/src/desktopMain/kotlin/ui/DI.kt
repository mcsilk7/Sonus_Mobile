package ui

import com.example.sonus.DependencyContainer
import com.example.sonus.DesktopDateFormatter
import com.example.sonus.DesktopNetworkMonitor
import com.example.sonus.db.getDatabaseBuilder
import data.DesktopSessionManager
import network.DesktopWireGuardManager
import java.io.File
import java.util.Properties

object DesktopDI {
    lateinit var container: DependencyContainer
    val sessionManager = DesktopSessionManager()

    fun init() {
        val database = getDatabaseBuilder().build()
        
        val props = Properties()
        var localPropsFile = File("local.properties")
        if (!localPropsFile.exists()) {
            // Spróbuj w katalogu nadrzędnym (jeśli uruchomiono z podprojektu :desktop)
            localPropsFile = File("../local.properties")
        }
        
        if (localPropsFile.exists()) {
            localPropsFile.inputStream().use { props.load(it) }
        }
        DesktopWireGuardManager.init(props)

        container = DependencyContainer(
            dao = database.musicDao(),
            networkMonitor = DesktopNetworkMonitor(),
            dateFormatter = DesktopDateFormatter(),
            getToken = { sessionManager.getToken() }
        )

        // Start background synchronization
        network.DesktopSyncManager.startSyncCycle(
            dao = container.dao,
            apiService = container.apiService,
            networkMonitor = container.networkMonitor,
            sessionManager = sessionManager
        )
    }
}
