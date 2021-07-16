# AmityRxRemoteMediator

We are the `RemoteMediator` for a DB + Network based `PagingData` stream which traiggers network requests to fetch more items with given filters as user scrolls, and automatically `insert` / `query` necessarily information into / from database, for example, tokens for fetching more pages later.

Another common difficulty of using `RemoteMediator` is once items are inserted into database, there is no easy way to tell which item has been deleted, updated or moved, so without a full data comparison or a reliable real-time event from a server we end up showing outdated data and I'll tell what? we don't need to worry about it on `AmityRxRemoteMediator`.

## First, pick the right mediator

We support 3 types of mediator, it depends on how do we fetch data from a pagined source.

#### Item-keyed Remote Mediator

using information from the items themselves to fetch more data.

#### Page-keyed Remote Mediator

using tokens to load pages (each response has next and previous tokens).

#### Positional Remote Mediator

using `skip` and `limit` to specify where to begin returning results and the maximum number of results to be returned.

## Item-keyed Remote Mediator

```code
TODO
```

## Page-keyed Remote Mediator

```code
abstract class AmityPageKeyedRxRemoteMediator<ENTITY:Any, TOKEN : AmityQueryToken, TOKEN_DAO : AmityQueryTokenDao<TOKEN>> {

    abstract fun fetchFirstPage(): Maybe<TOKEN>
        
    abstract fun fetch(token: TOKEN): Maybe<TOKEN>
        
    abstract fun primaryKeys(): Map<String, Any>
    
    abstract fun stackFromEnd(): Boolean
}
```

#### Sample

In this sample we assume we need to build a book store application with a simple paginated list of books with a filter function that allows user to only see a list of books with a specific title and category. First let's create a book `Entity` which has three arguments bookId, title and category and a book `Dao` with two basic functions, query and insert.

```code 
@Entity(
    tableName = "book",
    primaryKeys = ["bookId"],
    indices = [Index(value = ["title", "category"])]
)
class Book(var bookId: String, var title: String, var category: String)
``` 

```code 
@Dao
interface BookDao {

    @Query("select * from book where title = :title and category = :category")
    fun queryBooks(title: String, category: String): PagingSource<Int, Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBooks(books: List<Book>): Completable
}
``` 

### AmityQueryToken

`AmityQueryToken` is a `Room` entity designed to keep a next token and a previous token of each page which is later used for fetching more pages and refreshing existing pages. Create a new `Room` entity, make sure it extends `AmityQueryToken` and add more query parameters, if any. So we have the same set of query parameters on next queries.

What are query parameters? why do we need it? query parameters are a set of filters, in different usecases, sets of filters are most likely different, so are query results, so are tokens, this is why we need to keep a bond between query parameters and tokens because each set of query parameters has specific tokens.

**Note:** This is a very **IMPORTANT RULE**, we need to make sure that all query parameters are member of primary keys, espescially when we have a wide variety of query parameters (filters) like, for example, we have two `ListFragment`s and each has its own a seperate set of query parameters (filters), so we need to keep these two separate on database and primary keys tell them apart.

#### Sample

```code 
@Entity(
    tableName = "book_query_token",
    primaryKeys = ["title", "category"] // query parameters as primary keys
)
class BookQueryToken(var title: String, var category: String, next: String?, previous: String?) : AmityQueryToken(next, previous)
``` 

### AmityQueryTokenDao

In order for us to have access to tokens we need to get hands on its `Dao`, create a new `Dao` make sure it extends `AmityPagingTokenDao` and pass it on via a class contructor, implement all these following functions, any other additional sql queries and transactions are on the `Interface` already.

##### queryToken
    
Execute a `BookQueryToken` query, a query string is built for us by the `Interface` all we need to do is to annotate a function with `@RawQuery`.

##### insertToken
    
Insert a `BookQueryToken` object into database for later usages, using `OnConflictStrategy.REPLACE` as a conflict strategy is recommended as tokens may change over time and outdate tokens should be replaced.

##### tableName
    
A query token table name.

#### Sample

```code 
@Dao
interface BookQueryTokenDao : AmityQueryTokenDao<BookQueryToken> {

    @RawQuery(observedEntities = [BookQueryToken::class])
    override fun queryToken(query: SimpleSQLiteQuery): Maybe<BookQueryToken>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insertToken(token: BookQueryToken): Completable

    override fun tableName(): String {
        return "book_query_token"
    }
}
``` 

### AmityPageKeyedRxRemoteMediator

##### fetchFirstPage
    
Trigger a network request to fetch the first page to acquire the first next token (or the first previous token in case stackFromEnd is `True`).
    
##### fetch
    
Trigger a network request with a specific token.
    
##### primaryKeys

A key/value `Map` of query parameters.
    
##### stackFromEnd
    
set to `False` if the first page is on the top (top-down fetching) or `True` if the first page is on the bottom (bottom-up fetching)

#### Sample
    
```code 
class BookPageKeyedRxRemoteMediator(val title: String, val category: String, val bookDao: BookDao, tokenDao: BookQueryTokenDao) : AmityPageKeyedRxRemoteMediator<Book, BookQueryToken, BookQueryTokenDao>(tokenDao) {

    private fun fetchBooksByTitleAndCategory(title: String, category: String, pageSize: Int): Maybe<JsonObject> {
        // trigger a book network request by title and category
    }

    private fun fetchBooksByToken(token: String): Maybe<JsonObject> {
        // trigger a book network request for a next page/previous page. 
    }

    override fun fetchFirstPage(): Maybe<BookQueryToken> {
        return fetchBooksByTitleAndCategory(title, category, pageSize)
            .flatMap {
                // insert books into database and return a next token           
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(
                        Maybe.just(
                            BookQueryToken(
                                next = it.get("next").asString,
                                previous = null
                            )
                        )
                    )
            }
    }

    override fun fetch(token: BookQueryToken): Maybe<BookQueryToken> {
        return fetchBooksByToken(token)
            .flatMap {
                // insert books into database and return tokens           
                val books = it["books"].asJsonArray
                val type = object : TypeToken<List<Book>>() {}.type
                bookDao.insertBooks(Gson().fromJson(books, type))
                    .andThen(
                        Maybe.just(
                            BookQueryToken(
                                next = it.get("next").asString,
                                previous = it.get("previous").asString
                            )
                        )
                    )
            }
    }

    override fun primaryKeys(): Map<String, Any> {
        return mapOf(
            "title" to title,
            "category" to category
        )
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
            remoteMediator = BookRxRemoteMediator(
                title = "rxjava",
                category = "programing",
                bookDao = bookDao,
                tokenDao = bookQueryTokenDao
            )
        ) { bookDao.queryBooks(title = "rxjava", category = "programing") }.flowable

        pagingData
            .doOnNext { recyclerAdapter.submitData(this, it) }
            .subscribe()
```

**Note:** It is a very **IMPORTANT** that a database query and a network query are identical TODO TODO TODO

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
