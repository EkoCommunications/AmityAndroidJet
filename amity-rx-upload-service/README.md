# RxUpload Kotlin Extension

we are the kotlin extension under `Uri` class that allows user to upload a single object as a set of parts (Multipart upload) and be able to easily track the progress directly from a rx stream of upload progresses.

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

You can excute upload directly from `Uri` and you also have the access to file informations, bytes written and an upload progress directly from `Flowable` of `FileProperties`.

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

### Pre-xxx

## Base url

Define the base url of your application.

```text
RxUploadService.init("baseUrl" = baseUrl) 
```

## Inject Http intercepter

We also allow you to inject request intercepters.

```text
RxUploadService.init("interceptors" = interceptors) 
```

## Unsupport file type, maximun file size and timeouts

You are able to define a list of supported mime types and a maximum file size, please keep in mind that this is just only for a pre-validation on the extension, failure to comply will result in an error before uploading, any constrains on your backend side we have no control over that.

You also are able to define connect timeout, read timeout and write timeout.  

this is a optional step the extension accepts all mime types, up to 1000MB file size and 60s timeouts as default

```text
val settings = Settings.Builder
                    .supportedMimeTypes(mimeTypes)
                    .maximumFileSize(fileSize)
                    .connectTimeOutMillis(connectTimeOut)
                    .readTimeOutMillis(readTimeOut)
                    .writeTimeOutMillis(writeTimeOut)
                    .build()
                    
RxUploadService.init("settings" = settings) 
```

### Parameters

## Path

## Http header
```text
```

## Request body
```text
```

## Upload id
```text
``` 

## Cancel request
```text
```
