package com.aidanvii.toolbox.delegates.observable.rxjava

import android.support.annotation.CallSuper
import android.support.annotation.RestrictTo
import com.aidanvii.toolbox.delegates.observable.ObservableProperty
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <ST, TT> ObservableProperty<ST, TT>.toRx(compositeDisposable: CompositeDisposable? = null) =
    RxObservableProperty.SourceTransformer(this, compositeDisposable)

/**
 * Represents a [ReadWriteProperty] that can be observed of changes.
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 * @param TT the type on which this [ObservableProperty] operates.
 */
interface RxObservableProperty<ST, TT> : ReadWriteProperty<Any?, ST> {

    /**
     * Should be called when the final [RxObservableProperty] in the chain's [provideDelegate] operator method is called.
     *
     * Implementations of [RxObservableProperty] should call this in their [provideDelegate] operator method.
     *
     * When decorating, this should be forwarded to the decorated [RxObservableProperty]
     */
    @CallSuper
    fun onProvideDelegate(thisRef: Any?, property: KProperty<*>) {
    }

    val observable: Observable<ValueContainer<TT>>

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun subscribe(observable: Observable<*>)

    data class ValueContainer<T>(
        val property: KProperty<*>,
        val oldValue: T?,
        val newValue: T
    )

    class SourceTransformer<ST, TT>(
        private val decorated: ObservableProperty<ST, TT>,
        private val compositeDisposable: CompositeDisposable?
    ) : RxObservableProperty<ST, TT> {

        private val subject = BehaviorSubject.create<ValueContainer<TT>>()
        private var disposable: Disposable? = null
            set(value) {
                field?.dispose()
                field = value
                if (value != null && compositeDisposable != null) {
                    compositeDisposable.add(value)
                }
            }

        init {
            decorated.afterChangeObservers += { property, oldValue, newValue ->
                subject.onNext(ValueContainer(property, oldValue, newValue))
            }
        }

        override val observable: Observable<ValueContainer<TT>> get() = subject

        @RestrictTo(RestrictTo.Scope.LIBRARY)
        override fun subscribe(observable: Observable<*>) {
            disposable = observable.subscribe()
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>) = decorated.sourceValue

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: ST) {
            decorated.setValue(thisRef, property, value)
        }

        override fun onProvideDelegate(thisRef: Any?, property: KProperty<*>) {
            super.onProvideDelegate(thisRef, property)
            decorated.onProvideDelegate(thisRef, property)
        }
    }
}