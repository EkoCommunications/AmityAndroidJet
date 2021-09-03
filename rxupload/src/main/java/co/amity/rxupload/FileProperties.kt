package co.amity.rxupload

import android.net.Uri
import com.google.gson.JsonElement
import com.google.gson.JsonNull

data class FileProperties(
    val uri: Uri,
    val fileSize: Long,
    val fileName: String,
    val mimeType: String,
    var bytesWritten: Long = 0,
    var contentLength: Long = 0,
    var progress: Int = 0,
    var responseBody: JsonElement = JsonNull.INSTANCE
)