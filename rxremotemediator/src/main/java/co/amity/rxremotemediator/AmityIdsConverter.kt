package co.amity.rxremotemediator

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AmityIdsConverter {

    @TypeConverter
    fun fromJson(json: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(json, type)
    }

    @TypeConverter
    fun toJson(ids: List<String>): String {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().toJson(ids, type)
    }
}