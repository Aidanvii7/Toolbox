package com.aidanvii.toolbox.adapterviews.recyclerview

import androidx.recyclerview.widget.RecyclerView
import com.aidanvii.toolbox.databinding.NotifiableObservable
import com.aidanvii.toolbox.databinding.PropertyMapper
import com.aidanvii.toolbox.delegates.observable.ObservableProperty
import com.aidanvii.toolbox.unused
import kotlin.reflect.KProperty

/**
 * Creates a property delegate for properties of classes that implement [NotifiableObservable] and [AdapterNotifier] ([ObservableItemViewModel]).
 *
 * When a new value is assigned that is different from the current value, the [BindingRecyclerViewAdapter] will be notified to update.
 * Subsequently, [NotifiableObservable.notifyPropertyChanged] will be called within it's [RecyclerView.Adapter.onBindViewHolder] callback.
 *
 * This should be used when a property of a [NotifiableObservable]/[ObservableViewModel] needs reflected in the view whenever it changes.
 *
 * See: [ObservableItemViewModel] for more details.
 *
 * The property must be annotated with [Bindable].
 *
 * Usage:
 * ```
 * @get:Bindable
 * var firstName by adapterBindable("")
 * ```
 * @param fullRebind optional flag that determines whether a payload is supplied to [RecyclerView.Adapter.notifyItemChanged].
 * With a payload is more optimal as the same [RecyclerView.ViewHolder] will be reused, however the crossfade change animation will be affected
 * by [RecyclerView.ItemAnimator.canReuseUpdatedViewHolder].
 */
fun <ObservableAdapterNotifier, T> ObservableAdapterNotifier.adapterBindable(
    initialValue: T,
    fullRebind: Boolean = true
) where ObservableAdapterNotifier : NotifiableObservable, ObservableAdapterNotifier : AdapterNotifier =
    BindableAdapterProperty.Distinct(this, fullRebind, initialValue)

/**
 * Like [adapterBindable], but will trigger property change events even when the same value has been assigned, i.e. a value object with structural equality to the existing value object.
 *
 * This should be used sparingly, such as in cases when you need to send an event to the view (MVP style), and thus triggering any associated binding adapters.
 *
 * A classic case would be triggering finite view state from a view-model, such as triggering an animation on the view.
 *
 * Usage:
 * ```
 * @get:Bindable
 * var triggerAnimation by adapterBindableEvent(true)
 * ```
 * Regardless of whether [triggerAnimation] is assigned the same value, the binding adapter should fire.
 */
fun <ObservableAdapterNotifier, T> ObservableAdapterNotifier.adapterBindableEvent(
    initialValue: T,
    fullRebind: Boolean = true
) where ObservableAdapterNotifier : NotifiableObservable, ObservableAdapterNotifier : AdapterNotifier =
    BindableAdapterProperty.Distinct(this, fullRebind, initialValue)


sealed class BindableAdapterProperty<ObservableAdapterNotifier, T>(
    private val observableAdapterNotifier: ObservableAdapterNotifier,
    private val fullRebind: Boolean,
    initialValue: T
) : ObservableProperty.Source.Standard<T>(initialValue)
        where ObservableAdapterNotifier : NotifiableObservable, ObservableAdapterNotifier : AdapterNotifier {
    private var propertyId: Int = 0

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        observableAdapterNotifier.notifyAdapterPropertyChanged(propertyId, fullRebind)
    }

    operator fun provideDelegate(observable: NotifiableObservable, property: KProperty<*>): BindableAdapterProperty<ObservableAdapterNotifier, T> {
        propertyId = PropertyMapper.getBindableResourceId(property)
        return this
    }

    class Distinct<ObservableAdapterNotifier, T>(
        observable: ObservableAdapterNotifier,
        fullRebind: Boolean,
        initialValue: T
    ) : BindableAdapterProperty<ObservableAdapterNotifier, T>(observable, fullRebind, initialValue)
            where ObservableAdapterNotifier : NotifiableObservable, ObservableAdapterNotifier : AdapterNotifier {
        override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T) = oldValue != newValue
    }

    @Suppress(unused)
    class NonDistinct<ObservableAdapterNotifier, T>(
        observable: ObservableAdapterNotifier,
        fullRebind: Boolean,
        initialValue: T
    ) : BindableAdapterProperty<ObservableAdapterNotifier, T>(observable, fullRebind, initialValue)
            where ObservableAdapterNotifier : NotifiableObservable, ObservableAdapterNotifier : AdapterNotifier

}