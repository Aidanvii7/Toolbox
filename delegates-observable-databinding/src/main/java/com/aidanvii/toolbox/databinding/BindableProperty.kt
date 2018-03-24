package com.aidanvii.toolbox.databinding

import android.databinding.Bindable
import com.aidanvii.toolbox.delegates.observable.Functions
import com.aidanvii.toolbox.delegates.observable.ObservableProperty
import kotlin.reflect.KProperty

/**
 * Returns a source [ObservableProperty] ([ObservableProperty.Source]) for classes that implement [NotifiableObservable] ([ObservableViewModel]).
 *
 * When a new value is assigned that is different from the current value, [NotifiableObservable.notifyPropertyChanged] will be triggered.
 * This should be used when a property of a [NotifiableObservable]/[ObservableViewModel] needs reflected in the view whenever it changes.
 *
 * The property must be annotated with [Bindable].
 *
 * Usage:
 * ```
 * @get:Bindable
 * var firstName by bindable("")
 * ```
 *
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 */
fun <ST> NotifiableObservable.bindable(initialValue: ST): BindableProperty<ST> = BindableProperty(initialValue, this, Functions.areNotEqual)

/**
 * Returns a source [ObservableProperty] ([ObservableProperty.Source]) for classes that implement [NotifiableObservable] ([ObservableViewModel]).
 *
 * Unlike [bindable] this will trigger property change events even when the same value has been assigned,
 * i.e. a value object with structural equality to the existing value object.
 * This should be used sparingly, such as in cases when you need to send an event to the view (MVP style), and thus triggering any associated binding adapters.
 *
 * A classic case would be triggering finite view state from a view-model, such as displaying a toast or snackbar.
 *
 * Usage:
 * ```
 * @get:Bindable
 * var showToast by bindableEvent(true)
 * ```
 *
 * Regardless of whether [showToast] is assigned the same value, the binding adapter should fire.
 *
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 */
fun <ST> NotifiableObservable.bindableEvent(initialValue: ST): BindableProperty<ST> = BindableProperty(initialValue, this, Functions.alwaysEqual)

class BindableProperty<ST>(
        initialValue: ST,
        private val observable: NotifiableObservable,
        private val beforeChange: (oldValue: ST, newValue: ST) -> Boolean
) : ObservableProperty.Source<ST>(initialValue) {

    private var propertyId: Int = 0

    override fun afterChange(property: KProperty<*>, oldValue: ST, newValue: ST) {
        observable.notifyPropertyChanged(propertyId)
    }

    override fun onProvideDelegate(thisRef: Any?, property: KProperty<*>) {
        super.onProvideDelegate(thisRef, property)
        if (thisRef is NotifiableObservable) provideDelegate(thisRef, property)
    }

    operator fun provideDelegate(observable: NotifiableObservable, property: KProperty<*>): BindableProperty<ST> {
        propertyId = PropertyMapper.getBindableResourceId(property)
        return this
    }

    override fun beforeChange(property: KProperty<*>, oldValue: ST, newValue: ST) = beforeChange(oldValue, newValue)
}