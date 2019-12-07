package com.aidanvii.toolbox.adapterviews.databinding.recyclerview

import com.aidanvii.toolbox.Action
import com.aidanvii.toolbox.Provider
import com.aidanvii.toolbox.databinding.NotifiableObservable
import com.aidanvii.toolbox.delegates.weak.weak

interface AdapterNotifier {
    fun initAdapterNotifierDelegator(delegator: NotifiableObservable)
    fun notifyAdapterPropertyChanged(property: Int, fullRebind: Boolean)
    fun bindAdapter(adapter: BindingRecyclerViewAdapter<*>)
    fun unbindAdapter(adapter: BindingRecyclerViewAdapter<*>)
    fun adapterBindStart(adapter: BindingRecyclerViewAdapter<*>)
    fun adapterBindEnd(adapter: BindingRecyclerViewAdapter<*>)
    fun beginBatchedUpdates()
    fun endBatchedUpdates()

    class ChangePayload(
            val sender: AdapterNotifier,
            val changedProperties: IntArray
    )

    object delegate : Provider<AdapterNotifier> {

        private val NO_PENDING_CHANGES = intArrayOf()
        private const val FULL_REBIND = -1

        override fun invoke(): AdapterNotifier = AdapterNotifierImpl()

        private class AdapterNotifierImpl : AdapterNotifier {

            private lateinit var notifiableObservable: NotifiableObservable

            private var adapterHolders = mutableListOf<AdapterHolder>()

            override fun initAdapterNotifierDelegator(delegator: NotifiableObservable) {
                this.notifiableObservable = delegator
            }

            override fun notifyAdapterPropertyChanged(property: Int, fullRebind: Boolean) {
                if (adapterHolders.isNotEmpty()) {
                    val actualProperty = if (fullRebind) FULL_REBIND else property
                    adapterHolders.forEach { it.notifyAdapterPropertiesChanged(actualProperty) }
                } else {
                    notifiableObservable.notifyPropertyChanged(property)
                }
            }

            override fun bindAdapter(adapter: BindingRecyclerViewAdapter<*>) {
                if (adapterHolders.find { it.weakAdapter === adapter } == null) {
                    adapterHolders.add(AdapterHolder(adapter))
                }
            }

            override fun unbindAdapter(adapter: BindingRecyclerViewAdapter<*>) {
                adapterHolders.find { it.weakAdapter === adapter }?.let {
                    adapterHolders.remove(it)
                }
            }

            override fun adapterBindStart(adapter: BindingRecyclerViewAdapter<*>) {
                adapterHolders.find { it.weakAdapter === adapter }?.adapterBinding = true
            }

            override fun adapterBindEnd(adapter: BindingRecyclerViewAdapter<*>) {
                adapterHolders.find { it.weakAdapter === adapter }?.adapterBinding = false
            }

            override fun beginBatchedUpdates() {
                adapterHolders.forEach { it.notifyPaused = true }
            }

            override fun endBatchedUpdates() {
                adapterHolders.forEach { it.notifyPaused = false }
            }

            private inner class AdapterHolder(adapter: BindingRecyclerViewAdapter<*>) {

                private var pendingChangedProperties: IntArray = NO_PENDING_CHANGES
                var weakAdapter by weak(adapter)

                var adapterBinding = false
                var notifyPaused = false
                    set(value) {
                        if (field != value) {
                            field = value
                            if (!value) {
                                pendingChangedProperties.let {
                                    if (it.isNotEmpty()) {
                                        notifyAdapterPropertiesChanged(*it)
                                    }
                                }
                            }
                        }
                    }

                fun notifyAdapterPropertiesChanged(vararg properties: Int) {
                    if (properties.isNotEmpty() && !tryPostponePropertyChanges(properties)) {
                        if (adapterBinding) {
                            notifyAdapterPropertiesChangedDuringBindOrScrollOrLayout(properties)
                        } else {
                            notifyAdapterPropertiesChangedOutwithBind(properties)
                        }
                    }
                }

                private fun tryPostponePropertyChanges(properties: IntArray): Boolean {
                    return if (notifyPaused) {
                        pendingChangedProperties = (pendingChangedProperties + properties).distinct().toIntArray()
                        true
                    } else false
                }

                private fun notifyAdapterPropertiesChangedDuringBindOrScrollOrLayout(properties: IntArray) {
                    if (properties.contains(FULL_REBIND)) {
                        notifiableObservable.notifyChange()
                    } else {
                        for (property in properties) {
                            notifiableObservable.notifyPropertyChanged(property)
                        }
                    }
                }

                private fun notifyAdapterPropertiesChangedOutwithBind(properties: IntArray) {
                    weakAdapter?.apply {
                        getItemPositionFromBindableItem(notifiableObservable)?.let { adapterPosition ->
                            try {
                                if (properties.contains(FULL_REBIND)) {
                                    notifyItemChanged(adapterPosition)
                                } else {
                                    notifyItemChanged(adapterPosition, ChangePayload(this@AdapterNotifierImpl, properties))
                                }
                                pendingChangedProperties = NO_PENDING_CHANGES
                            } catch (e: IllegalStateException) {
                                notifyAdapterPropertiesChangedDuringBindOrScrollOrLayout(properties)
                            }
                        }
                    }
                }
            }
        }
    }
}

inline fun AdapterNotifier.batchUpdates(block: Action) {
    beginBatchedUpdates()
    block()
    endBatchedUpdates()
}