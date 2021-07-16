# AmityRemoteMediator

We are the `RemoteMediator` for a DB + Network based `PagingData` stream which traiggers network requests to fetch more items with given filters as user scrolls, and automatically `insert` / `query` necessarily information into / from database, for example, tokens for fetching more pages later.

Another common difficulty of using `RemoteMediator` is once items are inserted into database, there is no easy way to tell which item has been deleted, updated or moved, so without a full data comparison or a reliable real-time event from a server we end up showing outdated data and I'll tell what? you don't need to worry about it on `AmityRemoteMediator`.

## First, pick the right mediator

We support 3 types of mediator, it depends on how do you fetch data from a pagined source.

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
abstract class PageKeyedRemoteMediator<TOKEN : AmityQueryToken, TOKEN_DAO : AmityPagingTokenDao<TOKEN>> {

    abstract fun fetchFirstPage(): Maybe<TOKEN>
        
    abstract fun fetch(token: TOKEN): Maybe<TOKEN>
    
    abstract fun tableName(): String
    
    abstract fun primaryKeys(): Map<String, Any>
    
    abstract fun stackFromEnd(): Boolean
}
```

### Type arguments

##### AmityQueryToken

`AmityQueryToken` is a `Room` entity designed to keep a next token and a previous token of each page which is later used for fetching more pages and refreshing existing pages. Create a new `Room` entity, make sure it extends `AmityQueryToken` and add more query parameters, if any. So we have the same set of query parameters on next queries.

What are query parameters? why do we need it? query parameters are a set of filters, in different usecases, sets of filters are most likely different, so are query results, so are tokens, this is why we need to keep a bond between query parameters and tokens because each set of query parameters has specific tokens.

**Note:** This is a very **IMPORTANT RULE**, we need to make sure that all query parameters are member of primary keys, espescially when we have a wide variety of query parameters (filters) like, for example, we have two `ListFragment`s and each has its own a seperate set of query parameters (filters), so we need to keep these two separate on database and primary keys tell them apart.

##### AmityPagingTokenDao

In order for us to have access to tokens we need to get a hand on its Dao, create a new Dao make sure it extends AmityPagingTokenDao and pass it on via a class contructor, all required sql queries and transactions are on the Interface already.

### Functions overriding

##### fetchFirstPage
    
Trigger a network request to fetch the first page to acquire the first next and previous tokens.
    
##### fetch
    
Trigger a network request with a specific token.

##### tableName
    
A query token table name.
    
##### primaryKeys

A key/value `Map` of query parameters.
    
##### stackFromEnd
    
set to `False` if the first page is on the top (top-down fetching) or `True` if the first page is on the bottom (bottom-up fetching)
    
### Sample

##### BookQueryToken

```code 
class BookQueryToken(next: String?, previous: String?) : AmityQueryToken(next, previous)
``` 

##### BookQueryTokenDao

```code 
@Dao
interface BookQueryTokenDao : AmityPagingTokenDao<BookQueryToken>
``` 

##### BookRxRemoteMediator

```code 
class BookRxRemoteMediator(tokenDao: BookQueryTokenDao) : AmityRxRemoteMediator<BookQueryToken, BookQueryTokenDao>(tokenDao) {

    override fun fetchFirstPage(pageSize: Int): Maybe<BookQueryToken> {
        return Maybe.never<JsonObject>()
            .map {
                BookQueryToken(
                    next = it.get("next").asString,
                    previous = it.get("previous").asString
                )
            }
    }

    override fun fetchPage(token: BookQueryToken): Maybe<BookQueryToken> {
        return Maybe.never<JsonObject>()
            .map {
                BookQueryToken(
                    next = it.get("next").asString,
                    previous = it.get("previous").asString
                )
            }
    }

    override fun primaryKeys(): Map<String, Any> {
        return mapOf(
            "" to "",
            "" to ""
        )
    }

    override fun stackFromEnd(): Boolean {
        return true
    }
}
``` 

## Positional Remote Mediator

```code
abstract class PositionalRemoteMediator<PARAMS : AmityQueryParams, PARAMS_DAO : AmityQueryParamsDao<PARAMS>> {

    abstract fun fetch(skip: Int, limit: Int): Single<Array<PARAMS>>    

    abstract fun tableName(): String
    
    abstract fun primaryKeys(): Map<String, Any>
}
```

### Type arguments

##### AmityQueryParams

`AmityQueryParams` is a `Room` entity designed to keep query parameters (filters). Create a new `Room` entity, make sure it extends `AmityQueryParams` and add more query parameters, if any. So we have the same set of query parameters on next queries.

**Note:** This is a very **IMPORTANT RULE**, we need to make sure that all query parameters are member of primary keys, espescially when we have a wide variety of query parameters (filters) like, for example, we have two `ListFragment`s and each has its own a seperate set of query parameters (filters), so we need to keep these two separate on database and primary keys tell them apart.

##### AmityQueryParamsDao
    
In order for us to have access to query parameters we need to get a hand on its `Dao`, create a new `Dao` make sure it extends `AmityQueryParamsDao` and pass it on via a class contructor, all required sql queries and transactions are on the `Interface` already.
    
### Functions overriding
    
##### fetch

Trigger a network request with a specific length control by `skip` and `limit`.

##### tableName
    
A query parameter table name.
    
##### primaryKeys
    
A key/value `Map` of query parameters.
   
### Sample

TODO
    
## AmityPagingDataRefresher
    
As we mentioned in the beginning of this article, once items are inserted into database, `RemoteMediator` stops fetching any more items, without a full data comparison or a reliable real-time event from a server the items will eventually be outdated. To prevent that we need to inject `AmityPagingDataRefresher` into a `RecyclerView`. `AmityPagingDataRefresher` forces `RemoteMediator` to re-fetching items again when a user scrolls pass through pages. Update outdated items, get rid of deleted items or move items to new positions along with the process.
    
```code   
recyclerview.addOnScrollListener(AmityPagingDataRefresher())
```
