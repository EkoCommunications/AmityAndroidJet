# Amity Remote Mediator

We are the `RemoteMediator` for a DB + Network based `PagingData` stream which traiggers network requests to fetch more items with given filters as user scrolls, and `insert` / `query` required information into / from database to fetch previous pages, next pages and refresh a current page.

## Type of mediator

We support 3 types of mediator, it depends on how exactly do you query data from a pagined source.

#### Positional Remote Mediator

using `skip` and `limit` to specify where to begin returning results and the maximum number of results to be returned.

#### Item-keyed Remote Mediator

using information from the items themselves to fetch more data.

#### Page-keyed Remote Mediator

using tokens to load previous or next pages (each response has next/previous token).

## Positional Remote Mediator

```text
abstract class PositionalRemoteMediator<ENTITY : Any, PARAMS : AmityQueryParams, PARAMS_DAO : AmityQueryParamsDao<PARAMS>>(
    val context: Context, val paramsDao: PARAMS_DAO
)
```

#### ENTITY : Any

define a type of an item (`Room` entity).

#### PARAMS : AmityQueryParams

This is another `Room` entity required to keep query parameters (filters), extend and add more query parameters, if any. So we have the same set of query paramers on next queries.

**Note:** This is very importmant, we need to make sure that xxx are part of primary keys, xx

#### PARAMS_DAO : AmityQueryParamsDao<PARAMS>
    
TODO
    
#### Samples

On this sample, we assume we have access to network database and we need to fetch data from a pagined source and also numbers need to be appied.

## Item-keyed Remote Mediator

```text
TODO
```

## Page-keyed Remote Mediator

```text
TODO
```

#### Samples

TODO
