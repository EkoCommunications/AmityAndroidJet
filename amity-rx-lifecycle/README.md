# Bind a subscription with its holder's lifecycle!

## Rx Kotlin extensions

```text
fun <E> Flowable.untilLifecycleEnd(
    lifecycleProvider: LifecycleProvider<E>,
    uniqueId: String? = null
)

fun <E> Single.untilLifecycleEnd(
    lifecycleProvider: LifecycleProvider<E>,
    uniqueId: String? = null
)

fun <E> Maybe.untilLifecycleEnd(
    lifecycleProvider: LifecycleProvider<E>,
    uniqueId: String? = null
)

fun <E> Completable.untilLifecycleEnd(
    lifecycleProvider: LifecycleProvider<E>,
    uniqueId: String? = null
)
```

These extension are used for binding subscriptions with theirs holder's lifecycles. To prevent a memory leaks, subscriptions must be ended when theirs holder are destroyed!

### The life cycle provider

In order to have an access to the `LifeCycleProvider` your `Activity` and `Fragment` need to extend theirs base classes \(`RxAppCompatActivity`, `RxFragment` and etc.\) or else define your own lifecycle by implementing the `LifeCycleProvider` interface.

### The unique id

As you can see above, the `uniqueId` is optional. It requires when you wanna make sure that there should be **only one** active subscription under this particular `uniqueId`. For example, you need to make a network search function where it binds to user typing actions, for each action you need to make a network request but you don't want all of them to bind with the lifecycle just only the latest is enough so with the same `uniqueId` all those requests before the latest one will be canceled.

#### The old way

```text
var disposable: Disposable? = null

doAfterTextChanged {
    disposable?.dispose()
    disposable = searchFunctionSingle(it)
        .doOnNext { // search results }
        .bindToLifecycle(this)
        .subscribe()
    }
}
```

#### The new way

```text
doAfterTextChanged {
    // this line can be executed many times
    // but only the latest will remain binded with the lifecycle.   
    searchFunctionSingle(it)
        .doOnNext { // search results }
        .untilLifecycleEnd(lifecycleProvider = this, uniqueId = "id")
        .subscribe()
    }
}
```

### Bind to the Activity's lifecycle

```text
flowable.doOnNext { }
    .untilLifecycleEnd(lifecycleProvider = this)
    .subscribe()
```

The `Flowable` ends after the `Activity` reaches  `ON_DESTRY` state.

### Bind to the Fragment's lifecycle

```text
flowable.doOnNext { }
    .untilLifecycleEnd(lifecycleProvider = this)
    .subscribe()
```

The `Flowable` ends after the `Fragment` reaches  `ON_DESTRY` state.

### Bind to the View's lifecycle

Normally, in an ideal world we should avoid doing any subscriptions on a view level. It should only responsible for update the UI but as we all know, we live in the cruel world so here is how should subscriptions on the view level look like.

#### Custom view

```text
flowable.doOnNext { }
    .untilLifecycleEnd(view = this)
    .subscribe()
```

The `Flowable` ends after the `View` reaches  `ON_DETACHED` state.

As I mentioned earlier we should avoid doing this on the view level especially on the `RecyclerView`'s items but there come a time when it is unavoidable such as image loading :\( a user can scroll thru a list of users very fast. We gotta make sure that when an image of someone is loaded, it won't be displayed on someone else's item.

#### ViewHolder

```text
loadImageFunctionSingle(userId)
    .doOnSuccess { }
    .untilLifecycleEnd(view = holder.itemView)
    .subscribe()
```

The `Single` ends after the `ItemView` reaches `ON_DETACHED` state. it means the `ItemView` is being recycled and ready for another item or it can also means its parent \(the `RecyclerView`\) reaches `ON_DETACHED` state.

## Get to know how RxLifecycle from Trello works

{% embed url="https://github.com/trello/RxLifecycle" %}

### how does it work underneath?

`RxLifeCycle` provides `lifeCycleSubject` on the base `Activity` classes and the base `Fragment` classes which is used to bind to the `Observable` using `takeUntil` operation.  [\(Read more for takeUntil operation\)](http://reactivex.io/documentation/operators/takeuntil.html)

```text
flowable.compose(FlowableTransformer<T, T> { upstream ->
            upstream.takeUntil(lifeCycleSubject)
        })
        .doOnNext { }
        .subscribe()
```

#### bindToLifeCycle

This operation automatically detects which state of the `Activity` , the `Fragment` or the `View` you are on and tells where should the `Observable` ends. For example, if the current state is `ON_START` after applying the operation, the `Observable` ends after `lifeCycleSubject` emits `ON_STOP.`

#### bindUntilEvent

Unlike `bindToLifeCycle` It lets us tells it where should the `Observable` ends regardless of the current `Activity`, `Fragment` or `View` state you are on.
