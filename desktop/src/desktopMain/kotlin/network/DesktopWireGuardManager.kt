package network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
            val command = if (isFlatpak()) {
                listOf("flatpak-spawn", "--host", "sudo", "-n", "wg-quick", "--version")
            } else {
                listOf("sudo", "-n", "wg-quick", "--version")
            }
            val process = ProcessBuilder(command).start()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Próbuje uruchomić skrypt konfiguracyjny
     */
    fun runPermissionSetup() {
        try {
            val script = File("scripts/setup_vpn_perms.sh")
            if (script.exists()) {
                val command = if (isFlatpak()) {
                    listOf("flatpak-spawn", "--host", "pkexec", "sh", script.absolutePath)
                } else {
                    listOf("pkexec", "sh", script.absolutePath)
                }
                ProcessBuilder(command).start()
            }
        } catch (e: Exception) {
            println("Setup Error: ${e.message}")
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

            // Używamy tylko sudo. Jeśli nie ma NOPASSWD, polecenie po prostu się nie uda
            // zamiast pokazywać okno pkexec przy każdym starcie.
            val sudoTool = "sudo"

            // Wyjście z piaskownicy Flatpak na hosta (jeśli w niej jesteśmy)
            val command = if (isFlatpak()) {
                listOf("flatpak-spawn", "--host", sudoTool, "wg-quick", "up", configFilePath)
            } else {
                listOf(sudoTool, "wg-quick", "up", configFilePath)
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
            val sudoTool = "sudo"

            val command = if (isFlatpak()) {
                listOf("flatpak-spawn", "--host", sudoTool, "wg-quick", "down", configFilePath)
            } else {
                listOf(sudoTool, "wg-quick", "down", configFilePath)
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
