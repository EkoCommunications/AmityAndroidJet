package co.amity.rxupload

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import co.amity.rxupload.internal.repository.FileRepository
import io.reactivex.Flowable

class UploadFileUseCase {

    @WorkerThread
    fun upload(
        context: Context,
        uri: Uri,
        path: String,
        headers: Map<String, Any> = emptyMap(),
        params: Map<String, Any> = emptyMap(),
        id: String? = null,
        multipartDataKey: String
    ): Flowable<FileProperties> {
        return FileRepository().upload(context, uri, path, headers, params, id, multipartDataKey)
    }
}