# Amity RxLifecycle Kotlin Extensions

We are the kotlin extensions under `Flowable`, `Single`, `Maybe` and `Completable` that allow a user to easily one-lined bind any subscriptions to any lifecycles.

## PROBLEM!

Using `RxJava` is incredibly good (no doubt) but leaving a number of unused/unnecessary subscriptions active runs to a risk of slowness issues or memory leaking issues. Luckily, We have two handy libraries that make life much more easier. [RxLifeCycle](http://reactivex.io/documentation/operators/takeuntil.html) and [AutoDispose](https://uber.github.io/AutoDispose) are both serve a purpose of making sure that any subscriptions are not left active when they are no longer needed, but both come with limitations.

#### Mutable object

What happens if a parameter of a subscription is a mutable object?, when it mutates you would propably need to `dispose` of a current subscription and `subscribe` to a new one with a new updated parameter, this kind of process can happen again and again during a lifecycle and we only need to keep a latest subscription with a updated parameter. 

Keep an instance of a subscription and manually `dispose` is one way. This works perfectly fine but it definately destroys a beauty of one line magic, what will happen if there are multiple subscriptions in one class? not so handy any more huh?

Another way to solve this problem is to convert a mutable parameter to an active stream of data, aka "`Observable`" so you now are able to connect it with a later stream. This works perfectly fine as well in case you are in charge and be able to modify a source of data, you choose a solution for your problem.

```text
var disposable: Disposable? = null
var disposable2: Disposable? = null

textView.doAfterTextChanged {
    disposable?.dispose()
    disposable = functionASingle(it)
        .doOnSuccess { // do something }
        .bindToLifecycle(this)
        .subscribe()
    }
    
    disposable2?.dispose()
    disposable2 = functionBSingle(it)
        .doOnSuccess { // do something }
        .bindToLifecycle(this)
        .subscribe()
    }
}

// or 

BehaviorSubject.create<String> { subject ->
            textView.doAfterTextChanged {
                subject.onNext(string)
            }
        }
        .toFlowable(BackpressureStrategy.BUFFER)
        .flatMap { functionCFlowable(it) }
        .doOnNext { // do something }
        .bindToLifecycle(this)
        .subscribe()
```

**If both solutions are not good enough for you, keep reading!**

#### Unfinished tasks

What happens if tasks are left unfinished? `Single` and `Completable` are a kind of `Observable` that expects an item to be emitted or work to be done, if a lifecycle is ended before `Single` and `Completable` have a chance to do so (we find it true, a lot of time) an upstream will emit `CancellationException` without a proper error handler on a downstream, the app is crashed.

We find many cases on this scenario (not all) that an error handler likes an alert dialog or a toast is no use 'cause the lifecycle is ended. (a user has closed a particular screen that an error has occured and no one cares! but an error log may still be useful 🙂 likes I said, not all of the cases)

## WHAT WE OFFER!

We decide to extend the functionalities of `RxLifeCycle` to be able to solve previously mentioned problems and remain the beauty of one line magic that applicable to `Flowable`, `Single`, `Maybe` and `Completable` on `Actity`, `Fragment`, `View` and `ViewHolder`.

### Kotlin extensions

We introduce 4 kotlin extentions under `Flowable`, `Single`, `Maybe` and `Completable`.

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

These extension are used for binding any subscriptions with theirs holder's lifecycles. To prevent a memory leaks, subscriptions must be ended when theirs holders are destroyed!

### Parameters

#### lifecycleProvider

In order to have an access to the `LifeCycleProvider` your `Activity` and `Fragment` need to extend rx classes \(`RxAppCompatActivity`, `RxFragment` and etc.\) or else define your own lifecycle by implementing the `LifeCycleProvider` interface.

#### uniqueId (Optional)

As you can see above, the `uniqueId` is optional. It is required only when you wanna make sure that there should be **only one** active subscription under this particular `uniqueId`. For example, you need to make a network search function where it binds to user typing actions, for each action you need to make a network request but you don't want all of them to bind with the lifecycle just only the latest is enough so with the same `uniqueId` all those requests before the latest one will be canceled/disposed.

#### The old way

```text
var disposable: Disposable? = null

textView.doAfterTextChanged {
    disposable?.dispose()
    disposable = searchFunctionSingle(it)
        .doOnSuccess { // search results }
        .bindToLifecycle(this)
        .subscribe()
    }
}
```

#### The new way

```text
textView.doAfterTextChanged {
    // this line can be executed many times
    // but only the latest will remain binded with the lifecycle.   
    searchFunctionSingle(it)
        .doOnSuccess { // search results }
        .untilLifecycleEnd(lifecycleProvider = this, uniqueId = "id")
        .subscribe()
    }
}
```

### Cancellable Single and Completable

Without a proper error handler, if `Single` and `Completable` are disposed/canceled before it has a chance to finish thiers works. The app is crashed unless the following kotlin extensions are appied.

 ```text
single.doOnSuccess { }
    .allowEmpty()
    .untilLifecycleEnd(lifecycleProvider = this)
    .subscribe()
    
completable.doOnComplete { }
    .allowInComplete()
    .untilLifecycleEnd(lifecycleProvider = this)
    .subscribe()
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

### Long running tasks

We are aware that some of tasks are time-consuming tasks and we don't want them to bind to any lifecycles on some usecases, for example, a user tries to upload a big video file on one chat and switch to another chat while waiting, just because a fragment or a activity are closed/destroyed it doesn't always mean that an upload task should be canceled and we also don't want it to keep trying forever which could run to a risk of memory leaking issues. On this particular usecase we recommemd that you consider using `timeout` function from `RxJava` and make sure you handler `TimeoutException` properly when a specified timeout duration is reached.

```text
uploadVideoSingle()
    .timeout(60, TimeUnit.SECONDS)
    .doOnSuccess { // upload completed }
    .doOnError {
        if (it is TimeoutException) {
            // upload failed
        }
    }
    .subscribe()
```

## Get to know how RxLifecycle from Trello works

{% embed url="https://github.com/trello/RxLifecycle" %}

### How does it work underneath?

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
