package co.amity.rxupload.internal.datastore

import android.util.Log
import co.amity.rxupload.FileProperties
import co.amity.rxupload.service.MultipartUploadService
import co.amity.rxupload.service.api.MultipartUploadApi
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.reactivex.rxjava3.core.Flowable
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.URLEncoder
import kotlin.math.floor
import kotlin.math.min

class FileRemoteDataStore {

    fun upload(
        file: File,
        fileProperties: FileProperties,
        path: String,
        headers: Map<String, Any>,
        params: Map<String, Any>,
        id: String? = null,
        multipartDataKey: String
    ): Flowable<FileProperties> {
        return Flowable.fromPublisher<FileProperties> {
            val mediaType = fileProperties.mimeType.toMediaType()
            val requestBody = file
                .asRequestBody(mediaType)
                .asProgressRequestBody(object :
                    FileWritingListener {
                    override fun onWrite(bytesWritten: Long, contentLength: Long) {
                        val progress =
                            min(
                                floor(bytesWritten.toDouble() / contentLength.toDouble() * 100.toDouble()).toInt(),
                                99
                            )

                        it.onNext(fileProperties.apply {
                            this.bytesWritten = bytesWritten
                            this.contentLength = contentLength
                            this.progress = progress
                        })

                        MultipartUploadService.properties(id)?.onNext(fileProperties.apply {
                            this.bytesWritten = bytesWritten
                            this.contentLength = contentLength
                            this.progress = progress
                        })
                    }
                })

            val filename = fileProperties.fileName
            Log.e("FileRemoteDataStore", "upload: ${filename}")
            val disposition = buildString {
                append("form-data; name=")
                appendQuotedString(multipartDataKey)

                append("; filename=")
                appendQuotedString(filename)

                append("; filename*=")
                append('"')
                append("UTF-8")
                append('\'')
                append('\'')
                append(URLEncoder.encode(filename, "UTF-8"))
                append('"')
                //appendQuotedString("UTF-8''${URLEncoder.encode(filename, "UTF-8")}")

            }
            Log.e("FileRemoteDataStore", "disposition: ${disposition}")

            val partHeaders = Headers.Builder()
                .addUnsafeNonAscii("Content-Disposition", disposition)
                .build()

            val multipartBody = MultipartBody.Part.create(
                partHeaders,
                requestBody
            )

//            val multipartBody = MultipartBody.Part.createFormData(
//                multipartDataKey,
//                "ไทย.pdf",
//                requestBody
//            )

            val multipartUploadApi: MultipartUploadApi = MultipartUploadService.getUploadApi()

            val call = multipartUploadApi.upload(
                path,
                headers,
                multipartBody,
                params.mapValues { param -> param.value.toString().toRequestBody() })

            MultipartUploadService.onRequest(call, id)

            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    it.onError(t)
                    it.onComplete()
                    MultipartUploadService.onFailure(id)
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    response.errorBody()?.let { error ->
                        it.onError(Exception(JsonObject().apply {
                            addProperty("errorCode", response.code())
                            addProperty("errorBody", error.string())
                        }.toString()))
                    } ?: run {
                        it.onNext(fileProperties.apply {
                            response.body()?.string().let { jsonString ->
                                this.responseBody = JsonParser.parseString(jsonString)
                                this.progress = 100
                            }
                        })
                    }

                    it.onComplete()
                    MultipartUploadService.onResponse(id)
                }
            })
        }
    }

    internal fun StringBuilder.appendQuotedString(key: String) {
        append('"')
        for (i in 0 until key.length) {
            when (val ch = key[i]) {
                '\n' -> append("%0A")
                '\r' -> append("%0D")
                '"' -> append("%22")
                else -> append(ch)
            }
        }
        append('"')
    }

    companion object {

        fun RequestBody.asProgressRequestBody(listener: FileWritingListener): RequestBody {

            var bytesWritten: Long = 0

            return object : RequestBody() {
                override fun contentType(): MediaType? {
                    return this@asProgressRequestBody.contentType()
                }

                override fun contentLength(): Long {
                    return this@asProgressRequestBody.contentLength()
                }

                override fun writeTo(sink: BufferedSink) {
                    val forwardingSink = object : ForwardingSink(sink) {
                        override fun write(source: Buffer, byteCount: Long) {
                            super.write(source, byteCount)
                            bytesWritten += byteCount
                            listener.onWrite(bytesWritten, contentLength())
                        }
                    }.buffer()
                    this@asProgressRequestBody.writeTo(forwardingSink)
                    forwardingSink.flush()
                }
            }
        }
    }
}