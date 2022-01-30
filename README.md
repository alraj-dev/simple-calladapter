Simple Call Adapter
=

A Retrofit 2 `CallAdapter` implementation.

Usage
-

Add dependency:
In your project level `build.gradle`, add `jitpack` url:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

and in your module level `build.gradle`, add the dependency:
Latest Version: **`1.0`**

```groovy
dependencies {
        implementation 'com.github.alraj-dev:simple-calladapter:1.0'
}
```

Add the call adapter when building your `Retrofit` instance using the `addCallAdapter`

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://example.com/")
    .addCallAdapterFactory(SimpleCallAdapter.create())
    .build()
```

Simple Call:
-

An alternative for `Retrofit2`'s `Call<T>` with single callback function for simplicity. Of course this uses `Call` under the hood :P

Interface functions:
```kotlin
fun getUser(): SimpleCall<List<User>>
```

Use single function callback: **`SimpleCallback`**
`SimpleCallback.onResult()`
```kotlin
getUser().enqueue { response, exception, call ->
	// handle exception and response
}
```
`SimpleCallback` gives 3 parameters:

- response: T? - de-serialized class object
- exception: Throwable? - exception
- call: SimpleCall\<T\> - this call

you can retry a call using **call** object `call.retry()`, retry will use the same Callback given to `enqueue`, you can also otherwise give the retry function a separate callback.

You can also use other functions available in `Retrofit2`'s `Call`.

Conditional Response
--------------------

Simple Call Adapter givens two default conditions for Retrofit2 response, you can add it when creating the retrofit object.

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://example.com/")
    .addCallAdapterFactory(
        SimpleCallAdapter.create(
            arrayOf(NULL_DATA, EMPTY_LIST)
        )
    )
    .build()
```

- NULL_DATA: when response is `null`, returns `NullDataException` in callback and `null` for response. (I think Response can never be null, meh!)
- EMPTY LIST: when response is a `Collection`, `Map`, or `Array` and is empty, returns `EmptyListException` in callback and `null` for response.

You can also include or exclude the default conditions for individual calls
```kotlin
getUser().exclude(NULL_DATA).enqueue {}
getUser().include(NULL_DATA).enqueue {}
```

MultipleCall:
-

Call multiple SimpleCall API calls with a single response callback function.
eg:
```kotlin
for(userId in ids)
	getUserInfo(userId).enqueue {}
```

instead you can do it all within a single callback: **`MultipleCallback`**
`MultipleCallback.onResult()`
```kotlin
MultipleCall(
	ids.map { getUserInfo(it) }
).enqueue { responses, exceptions, calls, call ->
	// handle responses and exceptions
}
```

`MultipleCallback` gives 4 parameters:

- responses: List<T?>? - list of de-serialized class objects
- exceptions: List<Throwable?>? - list of exceptions
- calls: List<SimpleCall\<T\>> - list of given `SimpleCall`s
- call: MultipleCall\<T\> - this call representing the all the simple call series.

you can retry the whole list of calls with `call.retry()` or any individual calls with its own `SimpleCall` object. Same with `SimpleCall.retry()` you can give a separate Callback.
