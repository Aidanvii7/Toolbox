package com.aidanvii.toolbox.databinding

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.annotation.RestrictTo
import com.aidanvii.toolbox.Provider
/**
 * An abstraction of [BaseObservable] for composition purposes.
 *
 * The framework class [BaseObservable] forces inheritance.
 *
 * To use, a view model class should implement this and delegate to an instance of [BaseNotifiableObservable] like so:
 *
 * ```
 *  class ViewModel(
 *      // provide implementation as a default parameter, so a mock can be provided for tests.
 *      notifiableObservable: NotifiableObservable = NotifiableObservable.delegate()
 *  ) : NotifiableObservable by notifiableObservable {
 *
 *      init {
 *          // the delegatee requires a reference to it's delegator
 *          initDelegator(this)
 *      }
 *  }
 * ```
 *
 */
interface NotifiableObservable : Observable {

    /**
     * Must be set on initialisation. See [NotifiableObservable] for usage.
     */
    fun initDelegator(delegator: NotifiableObservable)

    /**
     * Notifies listeners that a specific property has changed.
     *
     * The getter for the property that changes should be marked with [Bindable]
     * so that field is generated in your `BR` class to be used as [propertyId].
     *
     * @param propertyId The generated BR id for the [Bindable] field.
     */
    fun notifyPropertyChanged(propertyId: Int)

    /**
     * Notifies listeners that all properties of this instance have changed.
     */
    fun notifyChange()

    object delegate :
            () -> NotifiableObservable,
            (LazyThreadSafetyMode) -> NotifiableObservable,
            (LazyThreadSafetyMode, Provider<PropertyChangeRegistry>) -> NotifiableObservable {
        private const val ALL_PROPERTIES = 0

        override fun invoke(): NotifiableObservable =
                NotifiableObservableImpl()

        override fun invoke(threadSafetyMode: LazyThreadSafetyMode): NotifiableObservable =
                NotifiableObservableImpl(threadSafetyMode)

        @RestrictTo(RestrictTo.Scope.TESTS)
        override fun invoke(threadSafetyMode: LazyThreadSafetyMode, registryProvider: Provider<PropertyChangeRegistry>): NotifiableObservable =
                NotifiableObservableImpl(threadSafetyMode, registryProvider)

        private class NotifiableObservableImpl(
                threadSafetyMode: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE,
                registryProvider: Provider<PropertyChangeRegistry> = { PropertyChangeRegistry() }
        ) : NotifiableObservable {

            private val changeRegistryDelegate = lazy(threadSafetyMode, registryProvider)
            private val changeRegistry: PropertyChangeRegistry by changeRegistryDelegate

            private lateinit var delegator: NotifiableObservable

            override fun initDelegator(delegator: NotifiableObservable) {
                this.delegator = delegator
            }

            override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
                changeRegistry.add(callback)
            }

            override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
                if (changeRegistryDelegate.isInitialized()) {
                    changeRegistry.remove(callback)
                }
            }

            override fun notifyChange() {
                if (changeRegistryDelegate.isInitialized()) {
                    changeRegistry.notifyChange(delegator, ALL_PROPERTIES)
                }
            }

            override fun notifyPropertyChanged(propertyId: Int) {
                if (changeRegistryDelegate.isInitialized()) {
                    changeRegistry.notifyChange(delegator, propertyId)
                }
            }
        }

    }
}