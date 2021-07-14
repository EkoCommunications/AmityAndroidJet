# Amity Remote Mediator

We are the `RemoteMediator` for a DB + Network based `PagingData` stream which traiggers network requests to fetch more items with given filters as user scrolls, and `insert` / `query` required information into / from database, for example, previous tokens or next tokens for fetching previous pages, next pages or refresh a current page later.

TODO
- find a missing item.
- insert a newly added item.

## Type of mediator

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
TODO
```

### Type arguments

#### ENTITY : Any

define a type of an item (`Room` entity).

#### Samples

TODO

## Positional Remote Mediator

```text
abstract class PositionalRemoteMediator<ENTITY : Any, PARAMS : AmityQueryParams, PARAMS_DAO : AmityQueryParamsDao<PARAMS>>(
    val context: Context, val paramsDao: PARAMS_DAO
)
```

### Type arguments

#### PARAMS : AmityQueryParams

This is another `Room` entity required to keep query parameters (filters), extend it and add more query parameters, if any. So we have the same set of query paramers on next queries.

**Note:** This is very **IMPORTANT RULE**, we need to make sure that all query parameters are member of primary keys, espescially when we have a wide variety of query parameters (filters) like, for example, we have two `ListFragment`s and each has its own a seperate set of query parameters, so we need to keep these two separate on database and primary keys tell them apart.

#### PARAMS_DAO : AmityQueryParamsDao<PARAMS>
    
TODO
    
#### Samples

TODO
