package com.example.tensorflowsample.image.processing

import org.json.JSONArray
import java.io.*
import java.net.URL


private val getVersionsLink = URL("https://api.github.com/repos/ArsenyChernyaev/TensorFlowSample/releases")

data class ModelInfo(val version: Int, val downloadUrl: String)

fun getListOfAllVersions(): List<ModelInfo> {
    return BufferedReader(InputStreamReader(getVersionsLink.openStream(), "UTF-8")).use { reader ->
        val releasesArray = JSONArray(reader.readText())
        (0..(releasesArray.length() - 1)).map { releasesArray.getJSONObject(it) }
            .map { releaseObject ->
                val assetObject = releaseObject.getJSONArray("assets").getJSONObject(0)
                ModelInfo(releaseObject.getInt("tag_name"), assetObject.getString("browser_download_url"))
            }
    }
}

fun downloadModel(modelInfo: ModelInfo, modelFile: File) {
    downloadFile(modelFile, modelInfo)
}

private fun downloadFile(
    destinationFile: File,
    versionInfo: ModelInfo
) {
    destinationFile.createNewFile()
    URL(versionInfo.downloadUrl).openStream().use { inputStream ->
        FileOutputStream(destinationFile).use { outputStream ->
            val buffer = ByteArray(1024)
            var length = inputStream.read(buffer)

            while (length > 0) {
                outputStream.write(buffer, 0, length)
                length = inputStream.read(buffer)
            }
            outputStream.flush()
        }
    }
}
