package com.aidanvii.toolbox.delegates.observable

import kotlin.reflect.KProperty

/**
 * Returns an [ObservableProperty] that causes the [ObservableProperty.Source]
 * to propagate it's initial item downstream.
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 */
fun <ST> ObservableProperty.Source<ST>.eager() = EagerDecorator(this)

class EagerDecorator<ST>(
        private val decorated: ObservableProperty.Source<ST>
) : ObservableProperty<ST, ST> by decorated {

    init {
        decorated.onProvideDelegateObservers += { property, oldValue, newValue ->
            decorated.notifyObservers(property, oldValue, newValue)
        }
        decorated.afterChangeObservers += { property, oldValue, newValue ->
            afterChangeObservers.forEach { it(property, oldValue, newValue) }
        }
    }

    override val afterChangeObservers = mutableSetOf<AfterChange<ST>>()

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): EagerDecorator<ST> {
        onProvideDelegate(thisRef, property)
        return this
    }
}