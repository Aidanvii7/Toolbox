package com.aidanvii.toolbox.delegates.observable

import androidx.annotation.CallSuper
import com.aidanvii.toolbox.Provider
import com.aidanvii.toolbox.unchecked
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Returns a source [ObservableProperty] ([ObservableProperty.Source]) that emits items given to it.
 * @param ST the base type of this source observable ([ObservableProperty.Source]).
 */
fun <ST> observable(initialValue: ST) = ObservableProperty.Source.Standard(initialValue)

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
     * The root/source of the [ObservableProperty] chain.
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

    abstract class Source<ST> : ObservableProperty<ST, ST> {

        /**
         * Implements the core logic of a [ObservableProperty].
         *
         * @param initialValue the initial value of the property.
         */
        open class Standard<ST>(initialValue: ST) : Source<ST>() {
            override var sourceValue = initialValue
        }

        @Suppress(unchecked)
        open class Lazy<ST>(
            private val initialValueProvider: Provider<ST>
        ) : Source<ST>() {
            private var _sourceValue: Any? = UNINITIALISED
            override var sourceValue
                get() = when (_sourceValue) {
                    UNINITIALISED -> initialValueProvider().also { _sourceValue = it }
                    else -> _sourceValue as ST
                }
                set(value) {
                    _sourceValue = value
                }

            private companion object {
                val UNINITIALISED = Any()
            }
        }

        abstract class WithProperty<ST>(
            private val beforeChange: (oldValue: ST, newValue: ST) -> Boolean
        ) : ObservableProperty.Source<ST>() {

            protected lateinit var property: KProperty<*>

            override fun onProvideDelegate(thisRef: Any?, property: KProperty<*>) {
                super.onProvideDelegate(thisRef, property)
                provideDelegate(thisRef, property)
            }

            operator fun provideDelegate(
                thisRef: Any?,
                property: KProperty<*>
            ) = apply { this.property = property }

            override fun beforeChange(property: KProperty<*>, oldValue: ST, newValue: ST) =
                beforeChange(oldValue, newValue)
        }

        abstract override var sourceValue: ST

        private var _afterChangeObservers: MutableSet<AfterChange<ST>>? = null
        private var _onProvideDelegateObservers: MutableSet<AfterChange<ST>>? = null

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

        var assignmentInterceptor: ((ST) -> ST)? = null

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
            val oldValue = sourceValue
            val interceptedValue = assignmentInterceptor?.let { it(value) } ?: value
            if (!beforeChange(property, oldValue, interceptedValue)) {
                return
            }
            sourceValue = interceptedValue
            afterChange(property, oldValue, interceptedValue)
            notifyObservers(property, oldValue, interceptedValue)
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