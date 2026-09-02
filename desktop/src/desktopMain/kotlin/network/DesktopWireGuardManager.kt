package network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ui.TerminalManager
import java.io.File
import java.util.Properties

object DesktopWireGuardManager {
    private const val TAG = "SonusVPN"
    private const val TUNNEL_NAME = "sonus_vpn"
    
    private var serverPublicKey = ""
    private var serverEndpoint = ""
    private var clientPrivateKey = ""

    private fun isFlatpak() = File("/.flatpak-info").exists()

    /**
     * Sprawdza, czy mamy uprawnienia do uruchomienia wg-quick bez hasła (sudo NOPASSWD)
     */
    /**
     * Sprawdza, czy w systemie istnieje użytkownik o danej nazwie
     */
    suspend fun checkSystemUserExists(username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("id", username).start()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun hasPasswordlessAccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Sprawdzamy czy mcsilk może wywołać coś jako sonus bez hasła
            val command = if (isFlatpak()) {
                listOf("flatpak-spawn", "--host", "sudo", "-u", "sonus", "-n", "true")
            } else {
                listOf("sudo", "-u", "sonus", "-n", "true")
            }
            val process = ProcessBuilder(command).start()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Próbuje uruchomić skrypt konfiguracyjny i zwraca wynik
     */
    suspend fun runPermissionSetup(): Boolean = withContext(Dispatchers.IO) {
        try {
            var script = File("scripts/setup_vpn_perms.sh")
            if (!script.exists()) {
                script = File("../scripts/setup_vpn_perms.sh")
            }
            
            if (!script.exists()) {
                TerminalManager.addLog("ERROR: SCRIPT_NOT_FOUND")
                println("VPN Setup Error: Script not found at ${script.absolutePath}")
                return@withContext false
            }

            TerminalManager.addLog("SCRIPT_FOUND: ${script.name}")
            val currentUser = System.getProperty("user.name") ?: "mcsilk"
            
            val command = if (isFlatpak()) {
                listOf("flatpak-spawn", "--host", "pkexec", "sh", script.absolutePath, currentUser)
            } else {
                listOf("pkexec", "sh", script.absolutePath, currentUser)
            }
            
            TerminalManager.addLog("AWAITING_AUTH...")
            val process = ProcessBuilder(command).start()
            
            // Log output in background
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            
            val exitCode = process.waitFor()
            TerminalManager.addLog("SETUP_EXIT_CODE: $exitCode")
            if (exitCode != 0) {
                println("Output: $output")
                println("Error: $error")
            }
            
            return@withContext exitCode == 0
        } catch (e: Exception) {
            println("Setup Error: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun init(props: Properties) {
        serverPublicKey = props.getProperty("WG_SERVER_PUBLIC_KEY") ?: ""
        serverEndpoint = props.getProperty("WG_SERVER_ENDPOINT") ?: ""
        clientPrivateKey = props.getProperty("WG_CLIENT_PRIVATE_KEY") ?: ""
    }

    suspend fun startVpn(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (clientPrivateKey.isBlank()) {
                println("VPN Start Error: Client Private Key is empty!")
                return@withContext false
            }

            val configContent = """
                [Interface]
                PrivateKey = $clientPrivateKey
                Address = 10.0.0.2/32
                MTU = 1200

                [Peer]
                PublicKey = $serverPublicKey
                Endpoint = $serverEndpoint
                AllowedIPs = 10.0.0.0/24
                PersistentKeepalive = 25
            """.trimIndent().replace("\r\n", "\n") + "\n"

            val configFilePath = "/tmp/$TUNNEL_NAME.conf"

            if (isFlatpak()) {
                // W Flatpaku musimy zapisać plik na hoście, aby wg-quick go widział
                val writeProcess = ProcessBuilder("flatpak-spawn", "--host", "sh", "-c", "cat > $configFilePath && chmod 600 $configFilePath")
                    .start()
                writeProcess.outputStream.use { it.write(configContent.toByteArray()) }
                writeProcess.waitFor()
            } else {
                val configFile = File(configFilePath)
                configFile.writeText(configContent)
                configFile.setReadable(false, false)
                configFile.setReadable(true, true)
                // W Linuxie lepiej użyć chmod przez ProcessBuilder dla pewności 600
                Runtime.getRuntime().exec(arrayOf("chmod", "600", configFilePath)).waitFor()
            }

            // Delegujemy do użytkownika 'sonus', który ma uprawnienia do wg-quick
            val sudoCommand = listOf("sudo", "-u", "sonus", "sudo", "wg-quick", "up", configFilePath)

            // Wyjście z piaskownicy Flatpak na hosta (jeśli w niej jesteśmy)
            val command = if (isFlatpak()) {
                listOf("flatpak-spawn", "--host") + sudoCommand
            } else {
                sudoCommand
            }

            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            println("VPN Start Output: $output")
            exitCode == 0
        } catch (e: Exception) {
            println("VPN Start Error: ${e.message}")
            false
        }
    }

    suspend fun stopVpn(): Boolean = withContext(Dispatchers.IO) {
        try {
            val configFilePath = "/tmp/$TUNNEL_NAME.conf"
            val sudoCommand = listOf("sudo", "-u", "sonus", "sudo", "wg-quick", "down", configFilePath)

            val command = if (isFlatpak()) {
                listOf("flatpak-spawn", "--host") + sudoCommand
            } else {
                sudoCommand
            }

            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            println("VPN Stop Error: ${e.message}")
            false
        }
    }
}
