package com.aidanvii.toolbox.databinding

import androidx.databinding.Bindable
import com.aidanvii.toolbox.Provider
import com.aidanvii.toolbox.delegates.observable.Functions
import com.aidanvii.toolbox.delegates.observable.ObservableProperty
import kotlin.reflect.KProperty


fun <ST> NotifiableObservable.bindableLazy(
    initialValueProvider: Provider<ST>
) = BindableLazyProperty(initialValueProvider, this, Functions.areNotEqual)

class BindableLazyProperty<ST>(
    initialValueProvider: Provider<ST>,
    private val observable: NotifiableObservable,
    private val beforeChange: (oldValue: ST, newValue: ST) -> Boolean
) : ObservableProperty.Source.Lazy<ST>(initialValueProvider) {

    private var propertyId: Int = 0

    override fun afterChange(property: KProperty<*>, oldValue: ST, newValue: ST) {
        observable.notifyPropertyChanged(propertyId)
    }

    override fun onProvideDelegate(thisRef: Any?, property: KProperty<*>) {
        super.onProvideDelegate(thisRef, property)
        if (thisRef is NotifiableObservable) provideDelegate(thisRef, property)
    }

    operator fun provideDelegate(
        observable: NotifiableObservable,
        property: KProperty<*>
    ): BindableLazyProperty<ST> {
        propertyId = PropertyMapper.getBindableResourceId(property)
        return this
    }

    override fun beforeChange(property: KProperty<*>, oldValue: ST, newValue: ST) =
        beforeChange(oldValue, newValue)
}