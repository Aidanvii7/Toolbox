package com.aidanvii.delegates.observable

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Returns a source [ObservableProperty] ([ObservableProperty.Source]) that emits items given to it.
 * @param ST the base type of this source observable ([ObservableProperty.Source]).
 */
fun <ST> observable(initialValue: ST) = ObservableProperty.Source(initialValue)

typealias AfterChange<T> = (property: KProperty<*>, oldValue: T?, newValue: T) -> Unit

/**
 * Represents a [ReadWriteProperty] that can be observed of changes.
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 * @param TT the type on which this [ObservableProperty] operates.
 */
interface ObservableProperty<ST, TT> : ReadWriteProperty<Any?, ST> {

    /**
     * The current value of the source observable ([ObservableProperty.Source]).
     */
    val sourceValue: ST

    /**
     * The root/source of the [ObservableProperty] chaim.
     */
    val source: Source<ST>

    /**
     * A set of [Observer] that will be invoked if [beforeChange] returns true.
     * Called after [doOnNext].
     */
    val afterChangeObservers: MutableSet<AfterChange<TT>>

    /**
     * Should be called when the final [ObservableProperty] in the chain's [provideDelegate] operator method is called.
     *
     * Implementations of [ObservableProperty] should call this in their [provideDelegate] operator method.
     *
     * When decorating, this should be forwarded to the decorated [ObservableProperty]
     */
    fun onProvideDelegate(thisRef: Any?, property: KProperty<*>) {
    }

    /**
     * Implements the core logic of a [ObservableProperty].
     *
     * @param initialValue the initial value of the property.
     */
    open class Source<ST>(initialValue: ST) : ObservableProperty<ST, ST> {

        private var _sourceValue: ST = initialValue
        private var _afterChangeObservers: MutableSet<AfterChange<ST>>? = null
        private var _onProvideDelegateObservers: MutableSet<AfterChange<ST>>? = null

        override val sourceValue get() = _sourceValue

        override val afterChangeObservers: MutableSet<AfterChange<ST>>
            get() = synchronized(this) {
                _afterChangeObservers.let {
                    it ?: mutableSetOf<AfterChange<ST>>().also { _afterChangeObservers = it }
                }
            }

        val onProvideDelegateObservers: MutableSet<AfterChange<ST>>
            get() = synchronized(this) {
                _onProvideDelegateObservers.let {
                    it ?: mutableSetOf<AfterChange<ST>>().also { _onProvideDelegateObservers = it }
                }
            }

        override val source: Source<ST> get() = this
        /**
         *  The callback which is called before a change to the property value is attempted.
         *  The value of the property hasn't been changed yet, when this callback is invoked.
         *  If the callback returns `true` the value of the property is being set to the new value,
         *  and if the callback returns `false` the new value is discarded and the property remains its old value.
         */
        protected open fun beforeChange(property: KProperty<*>, oldValue: ST, newValue: ST) = true

        /**
         * The callback which is called after the change of the property is made. The value of the property
         * has already been changed when this callback is invoked.
         */
        protected open fun afterChange(property: KProperty<*>, oldValue: ST, newValue: ST) {}

        override fun getValue(thisRef: Any?, property: KProperty<*>) = sourceValue

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: ST) {
            val oldValue = this.sourceValue
            if (!beforeChange(property, oldValue, value)) {
                return
            }
            this._sourceValue = value
            afterChange(property, oldValue, value)
            notifyObservers(property, oldValue, value)
        }

        override fun onProvideDelegate(thisRef: Any?, property: KProperty<*>) {
            super.onProvideDelegate(thisRef, property)
            if (_onProvideDelegateObservers != null) {
                onProvideDelegateObservers.forEach { observer ->
                    observer(property, null, sourceValue)
                }
            }
        }

        fun notifyObservers(property: KProperty<*>, oldValue: ST?, newValue: ST) {
            if (_afterChangeObservers != null) {
                afterChangeObservers.apply {
                    forEach { observer ->
                        observer(property, oldValue, newValue)
                    }
                }
            }
        }
    }
}