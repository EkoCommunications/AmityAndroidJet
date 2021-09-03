package co.amity.rxupload.internal.datastore

interface FileWritingListener {

    fun onWrite(bytesWritten: Long, contentLength: Long)
}