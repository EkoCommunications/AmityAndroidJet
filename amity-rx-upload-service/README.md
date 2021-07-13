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

You can excute upload directly from `Uri` and you also have the access to file informations, bytes written and an upload progress directly from `Flowable<FileProperties>`.

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

## Prerequisite

### Base url

Define the base url of your application.

```text
RxUploadService.init("baseUrl" = baseUrl) 
```

### Inject Http intercepter

We also allow you to inject request intercepters.

```text
RxUploadService.init("interceptors" = interceptors) 
```

### Unsupport file type, maximun file size and timeouts

You are able to define a list of supported mime types and a maximum file size, please keep in mind that this is just only for a pre-validation on the extension, failure to comply will result in an error before uploading, any constrains on your backend side we have no control over that.

You also are able to define connect timeout, read timeout and write timeout.  

**NOTE:** this is a optional step, the extension accepts all mime types, up to 1000MB file size and 60s timeouts by default

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

## Upload parameters

```text
Uri.upload(context: Context, 
            path: String,                             // a url path (baseUrl + path).
            headers: Map<String, Any> = emptyMap(),   // any required http headers specified in the `Map.
            params: Map<String, Any> = emptyMap(),    // any required parameters (request body) specified in the `Map`.
            id: String? = null,                       // an optional upload id used for cancel a request and acquire a request progress.
            multipartDataKey: String = "file"): Flowable<FileProperties>        // define a custome form-data
```

## Cancel upload

```text
RxUploadService.cancel(uploadId) 
```

**NOTE:** Dispose or cancel the `Flowable` from `upload` method won't stop uploading. it will sliently continue upload as normal.
