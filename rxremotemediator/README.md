# Amity RxRemoteMediator

We are the `RemoteMediator` for a DB + Network based `PagingData` stream which traiggers network requests to fetch more items as user scrolls, and automatically `insert` / `query` necessarily information to / from database, for example, tokens for fetching more pages.

Common challenges of using `RemoteMediator` is when items are inserted into database, there is no easy way to tell which item has been deleted, updated or moved, so without a full data comparison or a reliable real-time event from a server we end up showing invalid or outdated data and when items cannot be sorted by any of theirs variables but can only be sorted by some specific algorithms likes user preferences, best seller and etc. and those algorithms or sort keys are not passed on to us then we need to locally generate and store sort keys of each algorithms by ourself. On `AmityRxRemoteMediator` we offer solutions for both issues.

## First, pick the right mediator

We support 3 types of mediator, it depends on how do we fetch data from a pagined source.

#### ItemKeyedRxRemoteMediator

using information from the items themselves to fetch more data.

#### PageKeyedRxRemoteMediator

using tokens to load pages (each response has next and previous tokens).

#### PositionalRxRemoteMediator

using `skip` and `limit` to specify where to begin returning results and the maximum number of results to be returned.

## ItemKeyedRxRemoteMediator

```code
TODO
```

## PageKeyedRxRemoteMediator

```code
abstract class PageKeyedRxRemoteMediator<ENTITY : Any, TOKEN : AmityQueryToken>(val nonce: Int, val queryParameters: Map<String, Any> = mapOf(), val tokenDao: AmityQueryTokenDao) : AmityRxRemoteMediator<ENTITY>() {

    abstract fun fetchFirstPage(pageSize: Int): Maybe<TOKEN>
        
    abstract fun fetch(token: TOKEN): Maybe<TOKEN>
    
    abstract fun stackFromEnd(): Boolean
}
```

### Constructor parameters

##### Nonce

TODO

##### QueryParameters

A set of filters in the `Map`, if any. (Key/Value pairs)

##### `AmityQueryToken` and `AmityQueryTokenDao`

`AmityQueryToken` is an expected object returned by the abstract functions, it is designed to keep a set of query parameters in the `Map` (Key/Value pairs), next/previous tokens of each page which is later used for fetching more pages or refreshing existing pages and a set of unique ids of items of each page which is later used for identifying invalid items on database.

In order for us to have access to `AmityQueryToken` we need to get hands on `AmityPagingTokenDao`, make sure we define both on a `RoomDatabase` class and pass `AmityPagingTokenDao` to a class construtor.

### Abstract functions

##### fetchFirstPage(pageSize: Int)
    
Trigger a network request to fetch the first page to acquire the first next token (or the first previous token in case stackFromEnd is `True`).
    
##### fetch(token: TOKEN)
    
Trigger a network request with a specific token.
        
##### stackFromEnd()
    
set to `False` if the first page is on the top (top-down fetching) or `True` if the first page is on the bottom (bottom-up fetching)

### Sample

In this sample we assume we need to build a book store application with a simple paginated list of books with a filter function that allows a user to only see a list of books with a specific title and category. First, let's create a book `Entity` which has 3 variables: bookId, title and category as well as a book `Dao` with 2 basic functions: query and insert.

```code 
@Entity(
    tableName = "book",
    primaryKeys = ["bookId"],
    indices = [Index(value = ["title", "category"])]
)
class Book(var bookId: String, var title: String, var category: String) {

    companion object {
        const val NONCE: Int = 42
    }
}
``` 

```code 
@Dao
interface BookDao {

    @Query("select * from book where title = :title and category = :category order by title")
    fun queryBooks(title: String, category: String): PagingSource<Int, Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooks(books: List<Book>): Completable
}
``` 

Then define them on a database class along with `AmityQueryToken` and `AmityQueryTokenDao`.

```code 
@Database(entities = arrayOf(BookDao::class, AmityQueryToken::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun tokenDao(): AmityQueryTokenDao
}
``` 

Implement a new `PageKeyedRxRemoteMediator`. 

```code 
class BookQueryToken(var title: String, var category: String, next: String? = null, previous: String? = null, uniqueIds: List<String>) :
    AmityQueryToken(
        queryParameters = mapOf("title" to title, "category" to category),
        next = next,
        previous = previous,
        uniqueIds = uniqueIds
    )
``` 
    
```code 
class BookPageKeyedRxRemoteMediator(private val title: String, private val category: String, private val bookDao: BookDao, tokenDao: AmityQueryTokenDao) : PageKeyedRxRemoteMediator<Book, BookQueryToken>(
        nonce = Book.NONCE,
        queryParameters = mapOf("title" to title, "category" to category),
        tokenDao = tokenDao
    ) {

    private fun fetchBooksByTitleAndCategory(title: String, category: String, pageSize: Int): Single<JsonObject> {
        // trigger a book network request by title and category
    }

    private fun fetchBooksByToken(token: String): Single<JsonObject> {
        // trigger a book network request for a next page/previous page. 
    }

    override fun fetchFirstPage(pageSize: Int): Single<BookQueryToken> {
        return fetchBooksByTitleAndCategory(title, category, pageSize)
            .flatMap {
                // insert books into database and return token
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(
                        Single.just(
                            BookQueryToken(
                                title = title,
                                category = category,
                                next = it.get("next").asString,
                                previous = null,
                                uniqueIds = books.map { book -> book.asJsonObject["id"].asString }
                            )
                        )
                    )
            }
    }

    override fun fetch(token: String): Single<BookQueryToken> {
        return fetchBooksByToken(token)
            .flatMap {
                // insert books into database and return token
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(
                        Single.just(
                            BookQueryToken(
                                title = title,
                                category = category,
                                next = it.get("next").asString,
                                previous = it.get("previous").asString,
                                uniqueIds = books.map { book -> book.asJsonObject["id"].asString }
                            )
                        )
                    )
            }
    }
    
    override fun stackFromEnd(): Boolean {
        return false
    }
}
``` 

We now have everything in place, we can then proceed to create a `PagingData` stream using `RemoteMediator` and submit data into `RecyclerView` through its `Adapter`.

```code
        val pagingData = Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            initialKey = null,
            remoteMediator = BookPageKeyedRxRemoteMediator(
                title = "rxjava",
                category = "programing",
                bookDao = bookDao,
                tokenDao = tokenDao
            )
        ) { bookDao.queryBooks(title = "rxjava", category = "programing") }.flowable

        pagingData
            .doOnNext { recyclerAdapter.submitData(this, it) }
            .subscribe()
```

**Note:** It is a very **IMPORTANT** that a local database query and a network request are using the same set of parameters, using different sets of parameters on two datasources is very risky, `RemoteMediator` could repeatedly trigger a network request with one set of parameters while locally looking for data matched with another set of parameters which there is a posibility that there is no any or just some.

## Positional Remote Mediator

```code
abstract class PositionalRxRemoteMediator<ENTITY : Any, PARAMS : AmityQueryParams>(val nonce: Int, val queryParameters: Map<String, Any> = mapOf(), val paramsDao: AmityQueryParamsDao) : AmityRxRemoteMediator<ENTITY>() {

    abstract fun fetch(skip: Int, limit: Int): Single<PARAMS>    
}
```

### Constructor parameters

##### Nonce

TODO

##### QueryParameters

A set of filters in the `Map`, if any. (Key/Value pairs)

##### `AmityQueryParams` and `AmityQueryParamsDao`

`AmityQueryParams` is an expected object returned by the abstract function, it is designed to keep a set of query parameters in the `Map` (Key/Value pairs), a last page boolean flag and a set of unique ids of items of each page which is later used for identifying invalid items on database.

In order for us to have access to `AmityQueryParams` we need to get hands on `AmityQueryParamsDao`, make sure we define both on a `RoomDatabase` class and pass `AmityQueryParamsDao` to a class construtor.

### Abstract functions
    
##### fetch(skip: Int, limit: Int)

Trigger a network request with a specific length control by `skip` and `limit`.

### Sample

In this sample we assume we need to build a book store application with a simple paginated list of books with a filter function that allows a user to only see a list of books with a specific title and category. First, let's create a book `Entity` which has 3 variables: bookId, title and category as well as a book `Dao` with 2 basic functions: query and insert.

```code 
@Entity(
    tableName = "book",
    primaryKeys = ["bookId"],
    indices = [Index(value = ["title", "category"])]
)
class Book(var bookId: String, var title: String, var category: String) {

    companion object {
        const val NONCE: Int = 42
    }
}
``` 

```code 
@Dao
interface BookDao {

    @Query("select * from book where title = :title and category = :category order by title")
    fun queryBooks(title: String, category: String): PagingSource<Int, Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooks(books: List<Book>): Completable
}
``` 

Then define them on a database class along with `AmityQueryParams` and `AmityQueryParamsDao`.

```code 
@Database(entities = arrayOf(BookDao::class, AmityQueryToken::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun paramsDao(): AmityQueryParamsDao
}
``` 

Implement a new `PageKeyedRxRemoteMediator`. 

```code 
class BookQueryParams(var title: String, var category: String, endOfPaginationReached: Boolean, uniqueIds: List<String>) :
    AmityQueryParams(
        queryParameters = mapOf("title" to title, "category" to category),
        endOfPaginationReached = endOfPaginationReached,
        uniqueIds = uniqueIds
    )
``` 

```code 
class BookPositionalRxRemoteMediator(private val title: String, private val category: String, private val bookDao: BookDao, paramsDao: AmityQueryParamsDao) :
    PositionalRxRemoteMediator<Book, BookQueryParams>(
        nonce = Book.NONCE,
        queryParameters = mapOf("title" to title, "category" to category),
        paramsDao = paramsDao
    ) {

    private fun queryBySkipAndLimit(skip: Int, limit: Int): Single<JsonObject> {
        // trigger a book network request by skip and limit
    }

    override fun fetch(skip: Int, limit: Int): Single<BookQueryParams> {
        return queryBySkipAndLimit(skip, limit)
            .flatMap {
                // insert books into database and return params
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(
                        Single.just(
                            BookQueryParams(
                                title = title,
                                category = category,
                                endOfPaginationReached = books.size() < limit,
                                uniqueIds = books.map { book -> book.asJsonObject["id"].asString }
                            )
                        )
                    )
            }
    }
``` 

We now have everything in place, we can then proceed to create a `PagingData` stream using `RemoteMediator` and submit data into `RecyclerView` through its `Adapter`.


```code
        val pagingData = Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            initialKey = null,
            remoteMediator = BookPositionalRxRemoteMediator(
                title = "rxjava",
                category = "programing",
                bookDao = bookDao,
                paramsDao = paramsDao
            )
        ) { bookDao.queryBooks(title = "rxjava", category = "programing") }.flowable

        pagingData
            .doOnNext { recyclerAdapter.submitData(this, it) }
            .subscribe()
```

**Note:** It is a very **IMPORTANT** that a local database query and a network request are using the same set of parameters, using different sets of parameters on two datasources is very risky, `RemoteMediator` could repeatedly trigger a network request with one set of parameters while locally looking for data matched with another set of parameters which there is a posibility that there is no any or just some.

## Stay up-to-date and sorted
    
As we mentioned in the beginning of this article, once items are inserted into database, `RemoteMediator` stops fetching any more items, without a full data comparison or a reliable real-time event from a server the items will eventually be outdated. To prevent that we need to inject `AmityPagingDataRefresher` into a `RecyclerView`. `AmityPagingDataRefresher` forces `RemoteMediator` to re-fetching items again when a user scrolls pass through pages. Update outdated items, get rid of deleted items or move items to new positions along with the process.
    
```code   
recyclerview.addOnScrollListener(AmityPagingDataRefresher())
``` 

To make sure that outdated items get updated, invalid items won't be display and items stay sorted (if the books are sorted by thiers titles it should be okay but what happens if they are sorted by some kind of specific algorithms likes your preferences?).

Our simple `Dao` is no longer fit for a job, we need to adjust it by implementing `AmityPagingDao` and override a raw query function, generate a query string for the raw query function by calling `queryPagingData()` and pass these following parameters: a table name, a unique id, a nonce and query parameters in the `Map` (Key/Value pairs).

```code 
@Dao
interface BookDao : AmityPagingDao<Book> {

    @RawQuery(observedEntities = [Book::class, AmityPagingId::class])
    override fun queryPagingData(sqlQuery: SimpleSQLiteQuery): PagingSource<Int, Book>

    fun queryBooks(title: String, category: String): PagingSource<Int, Book> {
        return queryPagingData(
            generateSqlQuery(
                tableName = "book",
                uniqueIdKey = "bookId",
                nonce = Book.NONCE,
                queryParameters = mapOf("title" to title, "category" to category)
            )
        )
    }

//    @Query("select * from book where title = :title and category = :category order by title")
//    fun queryBooks(title: String, category: String): PagingSource<Int, Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooks(books: List<Book>): Completable
}
``` 
