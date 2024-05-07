package co.amity.rxupload.service

import co.amity.rxupload.FileProperties
import co.amity.rxupload.RxUploadService
import co.amity.rxupload.service.api.MultipartUploadApi
import io.reactivex.subjects.PublishSubject
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private lateinit var retrofit: Retrofit
private lateinit var settings: RxUploadService.Settings

class MultipartUploadService {

    companion object {

        private val calls = mutableMapOf<String, Call<ResponseBody>>()
        private val propertiesSubjects = mutableMapOf<String, PublishSubject<FileProperties>>()

        fun init(baseUrl: String, baseSettings: RxUploadService.Settings, interceptors: List<Interceptor>) {
            val httpClient = OkHttpClient.Builder()
                .also {
                    for (interceptor in interceptors) {
                        it.addInterceptor(interceptor)
                    }
                }
                .connectTimeout(baseSettings.connectTimeOutMillis, TimeUnit.MILLISECONDS)
                .readTimeout(baseSettings.readTimeOutMillis, TimeUnit.MILLISECONDS)
                .writeTimeout(baseSettings.writeTimeOutMillis, TimeUnit.MILLISECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

            settings = baseSettings
        }

        fun getUploadApi(): MultipartUploadApi {
            return retrofit.create(MultipartUploadApi::class.java)
        }

        fun getSettings(): RxUploadService.Settings {
            return settings
        }

        fun onRequest(call: Call<ResponseBody>, id: String?) {
            id?.let {
                propertiesSubjects[it] = propertiesSubjects[it] ?: PublishSubject.create<FileProperties>()
                calls[it] = call
            }
        }

        fun onFailure(id: String?) {
            id?.let {
                propertiesSubjects.remove(it)
                calls.remove(it)
            }
        }

        fun onResponse(id: String?) {
            id?.let {
                propertiesSubjects.remove(it)
                calls.remove(it)
            }
        }

        fun properties(id: String?): PublishSubject<FileProperties>? {
            return id?.let {
                propertiesSubjects[it] = propertiesSubjects[it] ?: PublishSubject.create<FileProperties>()
                propertiesSubjects[it]
            }
        }

        fun cancel(id: String) {
            calls.remove(id)?.cancel()
        }
    }
}