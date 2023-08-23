package co.amity.rxupload.internal.repository

import android.content.Context
import android.net.Uri
import co.amity.rxupload.FileProperties
import co.amity.rxupload.internal.datastore.FileLocalDataStore
import co.amity.rxupload.internal.datastore.FileRemoteDataStore
import co.amity.rxupload.service.MultipartUploadService
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import java.util.*

class FileRepository {

    fun upload(
        context: Context,
        uri: Uri,
        path: String,
        headers: Map<String, Any>,
        params: Map<String, Any>,
        id: String? = null,
        multipartDataKey: String
    ): Flowable<FileProperties> {
        val localDataStore = FileLocalDataStore()
        val remoteDataStore = FileRemoteDataStore()
        return Single.zip(localDataStore.getFileName(context, uri),
            localDataStore.getFileSize(context, uri)
                .flatMap {
                    return@flatMap when (it > MultipartUploadService.getSettings().maximumFileSize) {
                        true -> Single.error<Long>(
                            Exception(
                                String.format(
                                    "a file size is %s, the maximum file size is %s",
                                    it,
                                    MultipartUploadService.getSettings().maximumFileSize
                                )
                            )
                        )
                        false -> Single.just(it)
                    }
                },
            localDataStore.getMimeType(context, uri)
                .flatMap {
                    val supportedMimeTypes =
                        MultipartUploadService.getSettings().supportedMimeTypes
                    return@flatMap when (supportedMimeTypes.isNotEmpty() && !supportedMimeTypes.contains(
                        it
                    )) {
                        true -> Single.error<String>(
                            UnsupportedOperationException(
                                String.format(
                                    "the library doesn't support '%s' mime type",
                                    it
                                )
                            )
                        )
                        false -> Single.just(it)
                    }
                }
        ) { fileName, fileSize, mimeType ->
            FileProperties(
                uri,
                fileSize,
                fileName,
                mimeType
            )
        }
            .flatMapPublisher { fileProperties ->
                localDataStore.getFile(context, uri)
                    .flatMapPublisher {
                        remoteDataStore.upload(
                            it,
                            fileProperties,
                            path,
                            headers,
                            params,
                            id,
                            multipartDataKey
                        )
                    }
            }
            .doOnTerminate { localDataStore.clearCache(context) }
            .doOnCancel { /*do not clear cache here! just because the subscription is cancelled doesn't mean the upload is also cancel (silent upload!)*/ }
            .distinct { Objects.hash(it.progress, it.responseBody) }
    }
}