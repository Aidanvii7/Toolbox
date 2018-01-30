package tv.sporttotal.android.databinding

import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty

typealias Action = () -> Unit
typealias Consumer<T> = (T) -> Unit


inline fun <T> distinctObservable(initialValue: T,
                                  crossinline onNewValue: Consumer<T>) = object : DistinctObservableProperty<T>(initialValue) {

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = onNewValue(newValue)
}

class ValuePair<T>(val oldValue: T, val newValue: T)

inline fun <T> distinctObservablePair(initialValue: T,
                                      crossinline onNewValue: Consumer<ValuePair<T>>) = object : DistinctObservableProperty<T>(initialValue) {

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = onNewValue(ValuePair(oldValue, newValue))
}

/**
 * An implementation of [ObservableProperty] that performs an equality check before changing the internal value;
 * the change is propagated if they are not equal.
 */
open class DistinctObservableProperty<T>(initialValue: T) : ObservableProperty<T>(initialValue) {

    final override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T) = oldValue != newValue
}