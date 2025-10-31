package com.iml1s.xmrigminer.native

object XMRigBridge {
    init {
        System.loadLibrary("native-bridge")
    }

    external fun getVersion(): String
    external fun getCpuCores(): Int
    external fun getCpuInfo(): String
    external fun hasCryptoExtensions(): Boolean
}
