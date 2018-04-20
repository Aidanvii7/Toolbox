package com.aidanvii.toolbox.adapterviews.recyclerview

import android.support.v7.widget.RecyclerView
import android.view.View
import com.aidanvii.toolbox.DisposableItem
import com.aidanvii.toolbox.databinding.NotifiableObservable
import com.aidanvii.toolbox.databinding.ObservableViewModel
import com.aidanvii.toolbox.leakingThis
import java.util.concurrent.atomic.AtomicBoolean

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
abstract class ObservableItemViewModel(
    delegateNotifiableObservable: NotifiableObservable = NotifiableObservable.delegate(),
    delegateAdapterNotifier: AdapterNotifier = AdapterNotifier.delegate()
) :
    NotifiableObservable by NotifiableObservable.delegate(),
    AdapterNotifier by AdapterNotifier.delegate(),
    DisposableItem {

    final override val disposed = AtomicBoolean(false)

    init {
        delegateNotifiableObservable.initDelegator(this)
        delegateAdapterNotifier.initAdapterNotifierDelegator(this)
    }

    final override val isDisposed: Boolean
        get() = super.isDisposed

    final override fun dispose() {
        super.dispose()
    }
}