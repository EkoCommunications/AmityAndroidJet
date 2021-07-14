# AmityRemoteMediator

We are the `RemoteMediator` for a DB + Network based `PagingData` stream which traiggers network requests to fetch more items with given filters as user scrolls, and automatically `insert` / `query` necessarily information into / from database, for example, next tokens for fetching next pages later.

Another common difficulty of using `RemoteMediator` is once items are inserted into database, there is no easy way to tell which item has been deleted, updated or moved, so without a full data comparison or a reliable real-time event from server we end up showing outdated data and I'll tell what? you don't need to worry about it on `AmityRemoteMediator`.

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
abstract class PagedKeyedRemoteMediator<TOKEN : EkoQueryToken, TOKEN_DAO : AmityPagingTokenDao<TOKEN>> {

    abstract fun fetchFirstPage(): Maybe<TOKEN>
        
    abstract fun fetch(token: TOKEN): Maybe<TOKEN>
    
    abstract fun tableName(): String
    
    abstract fun primaryKeys(): Map<String, Any>
    
    abstract fun stackFromEnd(): Boolean
}
```

### Type arguments

##### EkoQueryToken

TODO

##### AmityPagingTokenDao<TOKEN>>

TODO

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

TODO

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

This is `Room` entity designed to keep query parameters (filters), create a new `Room` entity, make sure it extends `AmityQueryParams` and add more query parameters, if any. So we have the same set of query paramers on next queries.

**Note:** This is a very **IMPORTANT RULE**, we need to make sure that all query parameters are member of primary keys, espescially when we have a wide variety of query parameters (filters) like, for example, we have two `ListFragment`s and each has its own a seperate set of query parameters, so we need to keep these two separate on database and primary keys tell them apart.

##### AmityQueryParamsDao<PARAMS>
    
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
    
As we mentioned in the beginning of this article, once items are inserted into database, `RemoteMediator` stops fetching any more items, without a full data comparison or a reliable real-time event from server the items will eventually be outdated. To prevent that we need to inject `AmityPagingDataRefresher` into a `RecyclerView`. `AmityPagingDataRefresher` forces `RemoteMediator` to re-fetching items again when a user scrolls pass through pages. Update outdated items, get rid of deleted items or move items to new positions along with the process.
    
```code   
recyclerview.addOnScrollListener(AmityPagingDataRefresher())
```
