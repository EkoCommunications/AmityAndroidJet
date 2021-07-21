# Amity RxRemoteMediator

We are the `RxRemoteMediator` for a DB + Network based `PagingData` stream which traiggers network requests to fetch more items as user scrolls, and automatically `insert` / `query` necessarily information into / from database, for example, tokens for fetching more pages.

Another common difficulty of using `RxRemoteMediator` is once items are inserted into database, there is no easy way to tell which item has been deleted, updated or moved, so without a full data comparison or a reliable real-time event from a server we end up showing invalid or outdated data. TODO

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
}
```

### Constructor arguments

##### Nonce

TODO

##### QueryParameters

A set of filters in the `Map`, if any. (Key/Value pairs)

##### AmityQueryToken and AmityQueryTokenDao

`AmityQueryToken` is an expected object returned by the abstract functions, designed to keep next and previous tokens of each page which is later used for fetching more pages and refreshing existing pages, a set of unique ids of items of each page which is later used for identifying invalid items on database and a set of query parameters in the `Map`. (Key/Value pairs)

In order for us to have access to `AmityQueryToken` we need to get hands on `AmityPagingTokenDao`, make sure we define both on a `RoomDatabase` class and pass `AmityPagingTokenDao` to a class construtor.

### Abstract functions

##### fetchFirstPage
    
Trigger a network request to fetch the first page to acquire the first next token (or the first previous token in case stackFromEnd is `True`).
    
##### fetch
    
Trigger a network request with a specific token.
        
##### stackFromEnd
    
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

    private fun fetchBooksByTitleAndCategory(title: String, category: String, pageSize: Int): Maybe<JsonObject> {
        // trigger a book network request by title and category
    }

    private fun fetchBooksByToken(token: String): Maybe<JsonObject> {
        // trigger a book network request for a next page/previous page. 
    }

    override fun fetchFirstPage(pageSize: Int): Maybe<BookQueryToken> {
        return fetchBooksByTitleAndCategory(title, category, pageSize)
            .flatMap {
                // insert books into database and return token
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(
                        Maybe.just(
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

    override fun fetch(token: String): Maybe<BookQueryToken> {
        return fetchBooksByToken(token)
            .flatMap {
                // insert books into database and return token
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(
                        Maybe.just(
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
abstract class PositionalRemoteMediator<PARAMS : AmityQueryParams, PARAMS_DAO : AmityQueryParamsDao<PARAMS>> {

    abstract fun fetch(skip: Int, limit: Int): Single<Array<PARAMS>>

    abstract fun tableName(): String
    
    abstract fun primaryKeys(): Map<String, Any>
}
```

### AmityQueryParams

`AmityQueryParams` is a `Room` entity designed to keep query parameters (filters). Create a new `Room` entity, make sure it extends `AmityQueryParams` and add more query parameters, if any. So we have the same set of query parameters on next queries.

**Note:** This is a very **IMPORTANT RULE**, we need to make sure that all query parameters are member of primary keys, espescially when we have a wide variety of query parameters (filters) like, for example, we have two `ListFragment`s and each has its own a seperate set of query parameters (filters), so we need to keep these two separate on database and primary keys tell them apart.

#### Sample

```code 
TODO
``` 

### AmityQueryParamsDao
    
In order for us to have access to query parameters we need to get a hand on its `Dao`, create a new `Dao` make sure it extends `AmityQueryParamsDao` and pass it on via a class contructor, all required sql queries and transactions are on the `Interface` already.

#### Sample

```code 
TODO
``` 
    
### AmityPositionalRxRemoteMediator
    
##### fetch

Trigger a network request with a specific length control by `skip` and `limit`.

##### tableName
    
A query parameter table name.
    
##### primaryKeys
    
A key/value `Map` of query parameters.

#### Sample

```code 
TODO
``` 
    
## AmityPagingDataRefresher
    
As we mentioned in the beginning of this article, once items are inserted into database, `RemoteMediator` stops fetching any more items, without a full data comparison or a reliable real-time event from a server the items will eventually be outdated. To prevent that we need to inject `AmityPagingDataRefresher` into a `RecyclerView`. `AmityPagingDataRefresher` forces `RemoteMediator` to re-fetching items again when a user scrolls pass through pages. Update outdated items, get rid of deleted items or move items to new positions along with the process.
    
```code   
recyclerview.addOnScrollListener(AmityPagingDataRefresher())
```
