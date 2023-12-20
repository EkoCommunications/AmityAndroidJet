package co.amity.rxupload

import co.amity.rxupload.service.MultipartUploadService
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import java.util.*

private const val DEFAULT_CONNECT_TIMEOUT_MILLIS: Long = 30 * 1000
private const val DEFAULT_READ_TIMEOUT_MILLIS: Long = 60 * 1000
private const val DEFAULT_WRITE_TIMEOUT_MILLIS: Long = 10 * 60 * 1000
private const val DEFAULT_MAXIMUM_FILE_SIZE: Int = 1000 * 1000 * 1000

class RxUploadService {

    companion object {

        fun init(
            baseUrl: String,
            settings: Settings = Settings.build(),
            interceptors: List<Interceptor> = emptyList(),
            certificatePinner: CertificatePinner? = null
        ) {
            MultipartUploadService.init(baseUrl, settings, interceptors)
        }

        fun properties(id: String): Flowable<FileProperties> {
            return (MultipartUploadService.properties(id)?.toFlowable(BackpressureStrategy.BUFFER)
                ?: Flowable.never<FileProperties>())
                    .distinct { Objects.hash(it.progress, it.responseBody) }
        }
        
        fun invalidate(id: String) {
            MultipartUploadService.invalidate(id)
        }

        fun cancel(id: String) {
            MultipartUploadService.cancel(id)
        }
    }

    class Settings private constructor(
        val connectTimeOutMillis: Long,
        val readTimeOutMillis: Long,
        val writeTimeOutMillis: Long,
        val maximumFileSize: Int,
        val supportedMimeTypes: List<String>
    ) {

        companion object Builder {

            private var connectTimeOutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS
            private var readTimeOutMillis = DEFAULT_READ_TIMEOUT_MILLIS
            private var writeTimeOutMillis = DEFAULT_WRITE_TIMEOUT_MILLIS
            private var maximumFileSize = DEFAULT_MAXIMUM_FILE_SIZE
            private var supportedMimeTypes = emptyList<String>()

            fun connectTimeOutMillis(connectTimeOutMillis: Long): Builder {
                Builder.connectTimeOutMillis = connectTimeOutMillis
                return this
            }

            fun readTimeOutMillis(readTimeOutMillis: Long): Builder {
                Builder.readTimeOutMillis = readTimeOutMillis
                return this
            }

            fun writeTimeOutMillis(writeTimeOutMillis: Long): Builder {
                Builder.writeTimeOutMillis = writeTimeOutMillis
                return this
            }

            fun maximumFileSize(maximumFileSize: Int): Builder {
                Builder.maximumFileSize = maximumFileSize
                return this
            }

            fun supportedMimeTypes(supportedMimeTypes: List<String>): Builder {
                Builder.supportedMimeTypes = supportedMimeTypes
                return this
            }

            fun build(): Settings {
                return Settings(
                    connectTimeOutMillis,
                    readTimeOutMillis,
                    writeTimeOutMillis,
                    maximumFileSize,
                    supportedMimeTypes
                )
            }
        }
    }
}