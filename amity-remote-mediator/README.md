# Amity Remote Mediator

We are the `RemoteMediator` for a DB + Network based `PagingData` stream which traiggers network requests to fetch more items with given filters as user scrolls, and automatically `insert` / `query` necessarily information into / from database, for example, previous tokens or next tokens for fetching previous pages or next pages later.

Another common difficulty of using `RemoteMediator` is once items are inserted into database, there is no easy way to tell which item has been deleted, updated or moved, so without a full data comparison or a reliable real-time event from server we end up showing outdated data and I'll tell what? you don't need to worry about it on `AmityRemoteMediator`.

## First, pick the right mediator.

We support 3 types of mediator, it depends on how exactly do you query data from a pagined source.

#### Item-keyed Remote Mediator

using information from the items themselves to fetch more data.

#### Page-keyed Remote Mediator

using tokens to load previous or next pages (each response has next/previous token).

#### Positional Remote Mediator

using `skip` and `limit` to specify where to begin returning results and the maximum number of results to be returned.

## Item-keyed Remote Mediator

```text
TODO
```

## Page-keyed Remote Mediator

```text
abstract class PagedKeyedRemoteMediator<TOKEN : EkoQueryToken, TOKEN_DAO : AmityPagingTokenDao<TOKEN>> {

    abstract fun fetchFirstPage(pageSize: Int): Maybe<TOKEN>

    abstract fun fetchPage(pageNumber: Int, pageSize: Int): Maybe<TOKEN>
        
    abstract fun fetchNextPage(token: TOKEN, pageSize: Int): Maybe<TOKEN>
    
    open fun fetchPreviousPage(token: TOKEN, pageSize: Int): Maybe<TOKEN>

    abstract fun tableName(): String
    
    abstract fun primaryKeys(): Map<String, Any>
    
    abstract fun stackFromEnd(): Boolean
}
```

### Type arguments

##### TOKEN : EkoQueryToken

TODO

##### TOKEN_DAO : AmityPagingTokenDao<TOKEN>>

TODO

### Functions overriding

***fetchFirstPage:*** Trigger a network request to load the first page.
    
***fetchPage:*** Trigger a network request to load a specific page (refresh) to make sure that items stay updated, this is called by by `AmityPagingDataRefresher`

***fetchNextPage:*** Trigger a network request to load a next page when a user has reached the last page on database.

***fetchPreviousPage:*** Trigger a network request to load a previous page when a user has reached the last page on database.    

***tableName:*** A query token table name.
    
***primaryKeys:*** A key/value `Map` of query parameters.
    
***stackFromEnd:*** set to `False` if the first page is on the top (top-down fetching) or `True` if the first page is on the bottom (bottom-up fetching)

## Positional Remote Mediator

```text
abstract class PositionalRemoteMediator<PARAMS : AmityQueryParams, PARAMS_DAO : AmityQueryParamsDao<PARAMS>> {

    abstract fun fetch(skip: Int, limit: Int): Single<Array<PARAMS>>    

    abstract fun tableName(): String
    
    abstract fun primaryKeys(): Map<String, Any>
}
```

### Type arguments

##### PARAMS : AmityQueryParams

This is another `Room` entity required to keep query parameters (filters), create a new `Room` entity, make sure it extends `AmityQueryParams` and add more query parameters, if any. So we have the same set of query paramers on next queries.

**Note:** This is a very **IMPORTANT RULE**, we need to make sure that all query parameters are member of primary keys, espescially when we have a wide variety of query parameters (filters) like, for example, we have two `ListFragment`s and each has its own a seperate set of query parameters, so we need to keep these two separate on database and primary keys tell them apart.

##### PARAMS_DAO : AmityQueryParamsDao<PARAMS>
    
In order for us to have access to query parameters we need to get a hand on its `Dao`, create a new `Dao` make sure it extends `AmityQueryParamsDao` and pass it on via a class contructor, all required sql queries and transactions are on the `Interface` already.
    
### Functions overriding
    
***fetch:*** Trigger a network request with a specific length control by `skip` and `limit`.

***tableName:*** A query parameter table name.
    
***primaryKeys:*** A key/value `Map` of query parameters.
    
### Refresh
    
TODO
   
### Samples

TODO
