package co.amity.rxupload.service.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface MultipartUploadApi {

    // https://github.com/square/retrofit/issues/3275
    @JvmSuppressWildcards
    @Streaming
    @Multipart
    @POST("{path}")
    fun upload(
        @Path("path", encoded = true) path: String,
        @HeaderMap headers: Map<String, Any>,
        @Part body: MultipartBody.Part,
        @PartMap params: Map<String, RequestBody>
    ): Call<ResponseBody>
}