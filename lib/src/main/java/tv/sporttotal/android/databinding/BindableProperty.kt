package tv.sporttotal.android.databinding

import kotlin.reflect.KProperty


/**
 * Creates a [BindableProperty] for properties of classes that implement [NotifiableObservable].
 *
 * The property must be annotated with [Bindable].
 *
 * Usage:
 * ```
 * @get:Bindable
 * var firstName by bindable("")
 *
 * ```
 * @param bindableResourceId the id from the generated `BR` class. If omitted, the [PropertyMapper] must be initialised prior to usage.
 */
fun <T> NotifiableObservable.bindable(initialValue: T) = BindableProperty<T>(initialValue, this)


/**
 * Same as [bindable] with an extra function ([onFirstGet]) that is executed lazily the first time the property is accessed.
 *
 * @param initialValue the initial value of the property
 * @param threadSafetyMode the [LazyThreadSafetyMode] to use when executing the function [onFirstGet]
 * @param onFirstGet the action to invoke when the property is first accessed
 *
 * Usage:
 * ```
 * @get:Bindable
 * var data: List<String> by bindableLazy(emptyList()) {
 *    fetchDataAsync()
 * }
 *
 * ```
 */
inline fun <T> NotifiableObservable.bindableLazy(initialValue: T,
                                                 threadSafetyMode: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE,
                                                 crossinline onFirstGet: Action): BindableProperty<T> {
    return object : BindableProperty<T>(initialValue, this) {

        private val onFirstGetLazy by lazy(threadSafetyMode) { onFirstGet() }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            onFirstGetLazy
            return super.getValue(thisRef, property)
        }
    }
}

/**
 * A property delegate for properties of [NotifiableObservable] objects.
 */
open class BindableProperty<T>(initialValue: T,
                               private val observable: NotifiableObservable) : DistinctObservableProperty<T>(initialValue) {

    private var propertyId: Int = 0

    open fun afterNotify(property: KProperty<*>, oldValue: T, newValue: T) {}

    final override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        observable.notifyPropertyChanged(propertyId)
        afterNotify(property, oldValue, newValue)
    }

    operator fun provideDelegate(observable: NotifiableObservable, property: KProperty<*>): BindableProperty<T> {
        propertyId = PropertyMapper.getBindableResourceId(property)
        return this
    }
}
