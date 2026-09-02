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
            TerminalManager.addLog("LOCAL_PROPERTIES_LOADED")
        } else {
            TerminalManager.addLog("MISSING_PROPERTIES: VPN_DISABLED (Checked: ${File("local.properties").absolutePath})")
        }
        DesktopWireGuardManager.init(props)
        TerminalManager.addLog("SYSTEM_INITIALIZED")
        TerminalManager.addLog("READY_FOR_OPERATOR")

        container = DependencyContainer(
            dao = database.musicDao(),
            networkMonitor = DesktopNetworkMonitor(),
            dateFormatter = DesktopDateFormatter(),
            getToken = { sessionManager.getToken() }
        )
    }
}
