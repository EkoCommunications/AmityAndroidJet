# Amity Remote Mediator

We are the `RemoteMediator` for a DB + Network based `PagingData` stream which traiggers network requests to fetch more items with given filters as user scrolls, and `insert` required information into database and `query` required information from database to fetch previous pages, next pages and refresh a current page.

## Type of mediator

We support 3 types of mediator, it depends on how exactly do you query data from a pagined source.

### Positional Remote Mediator

using `skip` and `limit` to specify where to begin returning results and the maximum number of results to be returned.

### Item-keyed Remote Mediator (TODO)

using information from the items themselves to fetch more data.

### Page-keyed Remote Mediator

using tokens to load previous or next pages (each response has next/previous token).

## Positional Remote Mediator

```text
TODO
```

## Item-keyed Remote Mediator (TODO)

```text
TODO
```

## Page-keyed Remote Mediator

```text
TODO
```
