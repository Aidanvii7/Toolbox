package com.aidanvii.toolbox.adapterviews.recyclerview

import android.support.annotation.RestrictTo
import android.support.v7.widget.RecyclerView
import android.view.View
import com.aidanvii.toolbox.Provider
import com.aidanvii.toolbox.databinding.NotifiableObservable
import com.aidanvii.toolbox.databinding.PropertyMapper
import com.aidanvii.toolbox.leakingThis

/**
 * A convenience view-model class that implements [NotifiableObservable] and [AdapterNotifier].
 *
 * Use this in favour of [ObservableViewModel] when the view-model backs an item in a [RecyclerView],
 * and has properties that affect the layout of the bound [View], e.g. its width or height.
 *
 * When you have such a property, you can use the [adapterBindable] property delegate in favour of [bindable].
 * This will notify the [BindingRecyclerViewAdapter] that the item has changed, and will be re-bound via [RecyclerView.Adapter.onBindViewHolder],
 * allowing change animations to be triggered.
 */
@Suppress(leakingThis)
open class ObservableItemViewModel :
        NotifiableObservable by Factory.delegateNotifiableObservable(),
        AdapterNotifier by Factory.delegateAdapterNotifier() {

    init {
        initDelegator(this)
        initAdapterNotifierDelegator(this)
    }

    object Factory {

        private val defaultProvideNotifiableObservableDelegate: Provider<NotifiableObservable> = { NotifiableObservable.delegate() }
        private var provideNotifiableObservableDelegate = defaultProvideNotifiableObservableDelegate

        private val defaultProvideAdapterNotifierDelegate: Provider<AdapterNotifier> = { AdapterNotifier.delegate() }
        private var provideAdapterNotifierDelegate = defaultProvideAdapterNotifierDelegate

        internal fun delegateNotifiableObservable(): NotifiableObservable = provideNotifiableObservableDelegate()
        internal fun delegateAdapterNotifier(): AdapterNotifier = provideAdapterNotifierDelegate()

        @RestrictTo(RestrictTo.Scope.TESTS)
        fun <T : ObservableItemViewModel> tested(
                notifiableObservableDelegate: NotifiableObservable = delegateNotifiableObservable(),
                adapterNotifierDelegate: AdapterNotifier = delegateAdapterNotifier(),
                brClass: Class<*>,
                provideTested: Provider<T>): T {
            provideNotifiableObservableDelegate = { notifiableObservableDelegate }
            provideAdapterNotifierDelegate = { adapterNotifierDelegate }
            brClass.let { PropertyMapper.initBRClass(it, locked = false) }
            return provideTested().also {
                provideNotifiableObservableDelegate = defaultProvideNotifiableObservableDelegate
                provideAdapterNotifierDelegate = defaultProvideAdapterNotifierDelegate
            }
        }
    }
}