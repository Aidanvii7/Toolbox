package com.aidanvii.toolbox.delegates.observable

import kotlin.jvm.Volatile
import kotlin.reflect.KProperty

/**
 * Returns an [ObservableProperty] that skips the first [count] items emitted
 * by the receiver [ObservableProperty] and emits the remainder
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 * @param TT the type on which this [ObservableProperty] operates.
 */
fun <ST, TT> ObservableProperty<ST, TT>.skip(count: Int) = SkipDecorator(this, count)

class SkipDecorator<ST, TT>(
        private val decorated: ObservableProperty<ST, TT>,
        @Volatile
        private var countDown: Int
) : ObservableProperty<ST, TT> by decorated {

    private val countDownLock = Any()

    init {
        decorated.afterChangeObservers += { property, oldValue, newValue ->
            val shouldFire = synchronized(countDownLock) {
                if (countDown > 0) false.also { countDown-- } else true
            }
            if (shouldFire) {
                afterChangeObservers.forEach { it(property, oldValue, newValue) }
            }
        }
    }

    override val afterChangeObservers = mutableSetOf<AfterChange<TT>>()

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): SkipDecorator<ST, TT> {
        onProvideDelegate(thisRef, property)
        return this
    }
}