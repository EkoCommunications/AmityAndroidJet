package co.amity.rxremotemediator

interface AmityQueryDao {

    fun tableName(): String

    fun condition(primaryKeys: Map<String, Any>): String {
        return primaryKeys
            .map {
                when (val value = it.value) {
                    is String -> String.format("%s = '%s'", it.key, value)
                    is Boolean -> String.format("%s = '%s'", it.key, if (value) 1 else 0)
                    else -> String.format("%s = %s", it.key, it.value)
                }
            }
            .joinToString(separator = " and ")
    }
}