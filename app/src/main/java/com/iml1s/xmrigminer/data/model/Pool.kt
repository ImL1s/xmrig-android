package com.iml1s.xmrigminer.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pool(
    val name: String,
    val url: String,
    @SerialName("ssl_url") val sslUrl: String,
    val description: String,
    val fee: String
) {
    fun getUrl(useTls: Boolean): String = if (useTls) sslUrl else url
}
