package com.aidanvii.toolbox.adapterviews.databinding

import android.databinding.ViewDataBinding
import android.view.ViewGroup

class BindableAdapterDelegate<Item : BindableAdapterItem, VH : BindableAdapter.ViewHolder<*, Item>> {

    lateinit var bindableAdapter: BindableAdapter<Item, VH>

    fun onCreate(container: ViewGroup, viewType: Int): VH {
        return bindableAdapter.run {
            val layoutResourceId = viewTypeHandler.getLayoutId(viewType)
            val bindingResourceId = viewTypeHandler.getBindingId(layoutResourceId)
            val viewDataBinding = bindingInflater.run { container.unattachedBindingOf<ViewDataBinding>(layoutResourceId) }
            createWith(bindingResourceId, viewDataBinding).also { onCreated(it) }
        }
    }

    fun onBind(viewHolder: VH, adapterPosition: Int) {
        bindableAdapter.run {
            getItem(adapterPosition).let { adapterItem ->
                viewHolder.apply {
                    boundAdapterItem = adapterItem
                    if (!onInterceptOnBind(viewHolder, adapterPosition)) {
                        val bindableItem = adapterItem.lazyBindableItem.value
                        try {
                            viewDataBinding.setVariable(bindingResourceId, bindableItem)
                        } catch (classCastException: ClassCastException) {
                            throwBindableItemWrongType(adapterItem, bindableItem, classCastException)
                        }
                    }
                    onBindExtras(viewHolder, adapterPosition)
                    adapterItem.onBindExtras(viewHolder.viewDataBinding, adapterPosition)
                    viewDataBinding.executePendingBindings()
                }
                onBound(viewHolder, adapterPosition)
                adapterItem.onBound(adapterPosition)
                itemBoundListener?.invoke(adapterPosition)
            }
        }
    }

    fun onUnbind(viewHolder: VH, adapterPosition: Int) {
        bindableAdapter.run {
            if (!onInterceptUnbind(viewHolder, adapterPosition)) {
                viewHolder.viewDataBinding.setVariable(viewHolder.bindingResourceId, null)
            }
            onUnbindExtras(viewHolder, adapterPosition)
            viewHolder.boundAdapterItem?.onUnbindExtras(viewHolder.viewDataBinding, adapterPosition)
            viewHolder.viewDataBinding.executePendingBindings()
            onUnbound(viewHolder, adapterPosition)
            viewHolder.boundAdapterItem?.onUnBound(adapterPosition)
            viewHolder.boundAdapterItem = null
        }
    }

    fun onDestroy(viewHolder: VH, adapterPosition: Int) {
        bindableAdapter.run {
            if (viewHolder.boundAdapterItem != null) {
                onUnbind(viewHolder, adapterPosition)
            }
            onDestroyed(viewHolder, adapterPosition)
        }
    }

    private fun VH.throwBindableItemWrongType(adapterItem: Item, bindableItem: Any, cause: ClassCastException): Nothing =
            throw IllegalArgumentException(
                    "cannot set variable with type ${bindableItem::class.java.simpleName} on " +
                            "${viewDataBinding::class.java.simpleName} with binding variable ID " +
                            "provided by ${adapterItem::class.java.simpleName}.bindingId. " +
                            "Is ${adapterItem::class.java.simpleName}.layoutId correct?", cause
            )
}