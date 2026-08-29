package com.example.sonus.network

import com.google.gson.annotations.SerializedName

data class GithubRelease(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("body") val description: String,
    @SerializedName("assets") val assets: List<GithubAsset>
)

data class GithubAsset(
    @SerializedName("browser_download_url") val downloadUrl: String,
    @SerializedName("name") val name: String
)
