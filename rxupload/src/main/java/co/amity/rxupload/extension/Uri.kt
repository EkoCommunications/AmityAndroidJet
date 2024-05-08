package co.amity.rxupload.extension

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import co.amity.rxupload.FileProperties
import co.amity.rxupload.UploadFileUseCase
import io.reactivex.rxjava3.core.Flowable

@WorkerThread
fun Uri.upload(
    context: Context,
    path: String,
    headers: Map<String, Any> = emptyMap(),
    params: Map<String, Any> = emptyMap(),
    id: String? = null,
    multipartDataKey: String = "file"
): Flowable<FileProperties> {
    return UploadFileUseCase().upload(context, this, path, headers, params, id, multipartDataKey)
}