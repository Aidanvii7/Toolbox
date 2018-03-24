package com.aidanvii.delegates.observable

import kotlin.reflect.KProperty

/**
 * Returns an [ObservableProperty] that will transform the type [TT1] to [TT2] via the given [transform] function.
 * @param transform a transformational function to apply to each item emitted by the receiver [ObservableProperty].
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 * @param TT1 the type on which the receiver [ObservableProperty] operates.
 * @param TT2 the transformed type on which the resulting [ObservableProperty] operates (as dictacted be the [transform] function).
 */
infix fun <ST, TT1, TT2> ObservableProperty<ST, TT1>.map(
        transform: (TT1) -> TT2
) = MapDecorator(this, transform)

class MapDecorator<ST, TT1, TT2>(
        private val decorated: ObservableProperty<ST, TT1>,
        private val transform: (TT1) -> TT2
) : ObservableProperty<ST, TT2> {

    init {
        decorated.afterChangeObservers += { property, oldValue, newValue ->
            afterChangeObservers.forEach {
                it(property, oldValue?.let { transform(it) }, transform(newValue))
            }
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): ST =
            decorated.getValue(thisRef, property)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: ST) {
        decorated.setValue(thisRef, property, value)
    }

    override val sourceValue: ST get() = decorated.sourceValue

    override val source: ObservableProperty.Source<ST> get() = decorated.source

    override val afterChangeObservers = mutableSetOf<AfterChange<TT2>>()

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): MapDecorator<ST, TT1, TT2> {
        onProvideDelegate(thisRef, property)
        return this
    }
}