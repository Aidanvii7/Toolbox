package com.aidanvii.toolbox.delegates.observable.rxjava

import androidx.annotation.RestrictTo
import io.reactivex.Observable
import kotlin.reflect.KProperty

/**
 * Returns an [RxObservableProperty] that will transform the type [TT1] to [TT2] via the given [transform] function.
 * @param transform a transformational function to apply to each item emitted by the receiver [RxObservableProperty].
 * @param ST the base type of the source observable ([RxObservableProperty.SourceTransformer]).
 * @param TT1 the type on which the receiver [RxObservableProperty] operates.
 * @param TT2 the transformed type on which the resulting [RxObservableProperty] operates (as dictacted be the [transform] function).
 */
infix fun <ST, TT1, TT2> RxObservableProperty<ST, TT1>.map(
        transform: (TT1) -> TT2
) = RxMapDecorator(this, transform)

class RxMapDecorator<ST, TT1, TT2>(
        private val decorated: RxObservableProperty<ST, TT1>,
        private val transform: (TT1) -> TT2
) : RxObservableProperty<ST, TT2> {

    override val observable: Observable<RxObservableProperty.ValueContainer<TT2>>
        get() = decorated.observable.map {
            RxObservableProperty.ValueContainer(
                    property = it.property,
                    oldValue = it.oldValue?.let { transform(it) },
                    newValue = transform(it.newValue)
            )
        }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    override fun subscribe(observable: Observable<*>) {
        decorated.subscribe(observable)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): ST =
            decorated.getValue(thisRef, property)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: ST) {
        decorated.setValue(thisRef, property, value)
    }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): RxMapDecorator<ST, TT1, TT2> {
        onProvideDelegate(thisRef, property)
        subscribe(observable)
        return this
    }
}