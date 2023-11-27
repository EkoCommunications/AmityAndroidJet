package co.amity.rxupload.internal.datastore

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class FileLocalDataStore {

    private val cacheDirectory: String = "AMITY_RX_UPLOAD_SERVICE_CACHE"

    private fun isFile(uri: Uri): Boolean {
        return uri.scheme == null || uri.scheme == ContentResolver.SCHEME_FILE
    }

    private fun isContent(uri: Uri): Boolean {
        return uri.scheme == ContentResolver.SCHEME_CONTENT
    }

    private fun isDocument(context: Context, uri: Uri): Boolean {
        return DocumentsContract.isDocumentUri(context, uri)
    }

    private fun mimeTypeFromUri(context: Context, uri: Uri): String? {
        if (isFile(uri)) {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).toLowerCase(Locale.getDefault())
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }

        return context.contentResolver.getType(uri)
    }

    private fun fileNameFromUri(context: Context, uri: Uri): String? {
        if (isFile(uri)) {
            return uri.path?.let { File(it).name }
        }

        val contentResolver = context.contentResolver
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }

        return null
    }

    private fun fileSizeFromUri(context: Context, uri: Uri): Long? {
        if (isFile(uri)) {
            return uri.path?.let { File(it).length() }
        }
        val contentResolver = context.contentResolver
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        contentResolver.query(
            uri,
            arrayOf(OpenableColumns.SIZE),
            null,
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                return it.getLong(it.getColumnIndex(OpenableColumns.SIZE))
            }
        }

        return null
    }

    private fun fileFromUri(context: Context, uri: Uri): File? {
        if (isFile(uri)) {
            return uri.path?.let { File(it) }
        }

        return try {
            val contentResolver = context.contentResolver
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            contentResolver.openInputStream(uri)
                ?.use {
                    val directory = File(context.cacheDir, cacheDirectory)
                    directory.mkdirs()
                    val output = File(directory, UUID.randomUUID().toString())
                    it.copyTo(output.outputStream())
                    output
                }
        } catch (e: FileNotFoundException) {
            null
        }
    }

    fun getMimeType(context: Context, uri: Uri): Single<String> {
        return Single.fromPublisher {
            mimeTypeFromUri(context, uri)
                ?.let { mimeType ->
                    it.onNext(mimeType)
                    it.onComplete()
                } ?: run { it.onError(FileNotFoundException()) }
        }
    }

    fun getFileName(context: Context, uri: Uri): Single<String> {
        return Single.fromPublisher {
            fileNameFromUri(context, uri)
                ?.let { fileName ->
                    it.onNext(fileName)
                    it.onComplete()
                } ?: run { it.onError(FileNotFoundException()) }
        }
    }

    fun getFileSize(context: Context, uri: Uri): Single<Long> {
        return Single.fromPublisher {
            fileSizeFromUri(context, uri)
                ?.let { fileSize ->
                    it.onNext(fileSize)
                    it.onComplete()
                } ?: run { it.onError(FileNotFoundException()) }
        }
    }

    fun getFile(context: Context, uri: Uri): Single<File> {
        return Single.fromPublisher {
            fileFromUri(context, uri)
                ?.let { file ->
                    it.onNext(file)
                    it.onComplete()
                } ?: run { it.onError(FileNotFoundException()) }
        }
    }

    fun clearCache(context: Context): Completable {
        return Completable.fromAction {
            val directory = File(context.cacheDir, cacheDirectory)
            directory.deleteRecursively()
        }
    }
}