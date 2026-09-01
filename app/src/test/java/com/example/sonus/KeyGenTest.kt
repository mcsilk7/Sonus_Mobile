package com.example.sonus

import com.wireguard.crypto.KeyPair
import org.junit.Test

class KeyGenTest {
    @Test
    fun generateKeys() {
        val clientKeyPair = KeyPair()
        val clientPrivate = clientKeyPair.privateKey
        val clientPublic = clientKeyPair.publicKey
        
        val serverKeyPair = KeyPair()
        val serverPrivate = serverKeyPair.privateKey
        val serverPublic = serverKeyPair.publicKey
        
        println("--- NEW KEYS ---")
        println("CLIENT_PRIVATE_KEY=${clientPrivate.toBase64()}")
        println("CLIENT_PUBLIC_KEY=${clientPublic.toBase64()}")
        println("SERVER_PRIVATE_KEY=${serverPrivate.toBase64()}")
        println("SERVER_PUBLIC_KEY=${serverPublic.toBase64()}")
        println("----------------")
    }
}
