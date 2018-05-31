package com.aidanvii.toolbox.delegates.observable.rxjava

import com.aidanvii.toolbox.Action
import com.aidanvii.toolbox.Consumer
import com.aidanvii.toolbox.actionStub
import com.aidanvii.toolbox.consumerStub
import com.aidanvii.toolbox.delegates.observable.AfterChange
import com.aidanvii.toolbox.delegates.observable.ObservableProperty
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.plugins.RxJavaPlugins
import kotlin.reflect.KProperty

private val onErrorStub: Consumer<Throwable> = { RxJavaPlugins.onError(OnErrorNotImplementedException(it)) }

fun <ST> ObservableProperty.Source<ST>.subscribeTo(
    observable: Observable<ST>,
    compositeDisposable: CompositeDisposable,
    onError: Consumer<Throwable> = onErrorStub,
    onComplete: Action = actionStub
) = SubscribeDecorator(this, observable, { compositeDisposable.add(it) }, onError, onComplete)

/**
 * Returns an [ObservableProperty] that subscribes to a given [Observable], and updates the [ObservableProperty.Source]
 * when the given [Observable] emits a new value.
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 */
fun <ST> ObservableProperty.Source<ST>.subscribeTo(
    observable: Observable<ST>,
    outDisposable: Consumer<Disposable> = consumerStub,
    onError: Consumer<Throwable> = onErrorStub,
    onComplete: Action = actionStub
) = SubscribeDecorator(this, observable, outDisposable, onError, onComplete)

class SubscribeDecorator<ST>(
    private val decorated: ObservableProperty.Source<ST>,
    observable: Observable<ST>,
    consumeDisposable: Consumer<Disposable> = consumerStub,
    onError: Consumer<Throwable> = onErrorStub,
    onComplete: Action
) : ObservableProperty<ST, ST> by decorated {

    init {
        decorated.onProvideDelegateObservers += { property, oldValue, newValue ->
            consumeDisposable(observable.subscribe({ decorated.setValue(null, property, newValue) }, onError, onComplete))
        }

        decorated.afterChangeObservers += { property, oldValue, newValue ->
            afterChangeObservers.forEach { it(property, oldValue, newValue) }
        }
    }

    override val afterChangeObservers = mutableSetOf<AfterChange<ST>>()

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): SubscribeDecorator<ST> {
        onProvideDelegate(thisRef, property)
        return this
    }
}