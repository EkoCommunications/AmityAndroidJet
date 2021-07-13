# RxUpload Kotlin Extension

we are the kotlin extension under `Uri` class that allows user to upload a single object as a set of parts (Multipart upload) and be able to easily track the progress directly from a rx stream of progresses.

## HOW!

```text
Uri.upload(context, path, headers, params): Flowable<FileProperties>
```

```text
data class FileProperties(
    val uri: Uri,
    val fileSize: Long,
    val fileName: String,
    val mimeType: String,
    var bytesWritten: Long,
    var contentLength: Long,
    var progress: Int,
    var responseBody: JsonElement
)
```

You can excute upload directly from `Uri` and you also have the access to file informations, bytes written and an upload progress directly from `Flowable` of `FileProperties`

```text
//Fragment A

uri.upload()
   .doOnNext() { fileProperties: FileProperties ->          
      if (it.progress == 100) {
          // it.responseBody
      }
   }
   .doOnComplete() { // done }
   .doOnError() { // failed }
   .subscribe()
```

or

```text
val uploadId = "id"

//Fragment A

uri.upload("id" = uploadId)
   .subscribe()
   
//Fragment B

RxUploadService.properties(uploadId)
   .doOnNext() { fileProperties: FileProperties ->          
      if (it.progress == 100) {
          // it.responseBody
      }
   }
   .doOnComplete() { // done }
   .doOnError() { // failed }
   .subscribe()
```



## Upload Url
TODO

## HTTP Header
TODO

## Request Body
TODO

## Upload Id
TODO 

## Unsupport file type, maximun File size and timeouts
TODO

## Cancel upload
TODO
