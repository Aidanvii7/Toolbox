package com.aidanvii.toolbox.arch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.aidanvii.toolbox.unchecked

/**
 * Implementation of [ViewModelProvider.Factory] that takes care of the slightly awkward generic method [ViewModelProvider.Factory.create]
 *
 * It removes the need for the client to manually handle the given ViewModel [Class] in an if else clause or switch statement, or perform type casting when returning.
 *
 * Example:
 *
 * Suppose you have a [ViewModel] with a non-default constructor, where the [TypedFactory] for [MyViewModel] is declared inside such as:
 * ```
 * class MyViewModel(val someData: String) : ViewModel() {
 *      class Factory() : ViewModelFactory.TypedFactory<MyViewModel> {
 *          override fun create() = MyViewModel("hello world")
 *      }
 * }
 * ```
 * To create a [ViewModelFactory] with this [TypedFactory]:
 * ```
 * val factory = ViewModelFactory.Builder()
 *          .addTypedFactory(MyViewModel.Factory())
 *          .build()
 * ```
 */
class ViewModelFactory private constructor(
    private val classFactoryMap: Map<Class<out ViewModel>, TypedFactory<*>>
) : ViewModelProvider.Factory {

    interface TypedFactory<out T : ViewModel> {
        fun create(): T
    }

    class Builder() {
        val typedFactories = mutableMapOf<Class<out ViewModel>, TypedFactory<*>>()

        fun build() = ViewModelFactory(typedFactories.toMap())
    }

    @Suppress(unchecked)
    override fun <T : ViewModel> create(viewModelClass: Class<T>): T =
        classFactoryMap[viewModelClass]?.create() as? T ?: throwNoFactoryInstalled(viewModelClass)

    private fun throwNoFactoryInstalled(viewModelClass: Class<*>): Nothing =
        throw UnsupportedOperationException("No view-model factory installed for class: $viewModelClass")
}

inline fun <reified T : ViewModel> ViewModelFactory.Builder.addTypedFactory(factory: ViewModelFactory.TypedFactory<T>) = this.apply {
    typedFactories[T::class.java] = factory
}

fun androidx.fragment.app.FragmentActivity.viewModelProvider(factory: ViewModelProvider.Factory? = null): ViewModelProvider =
    if (factory != null) ViewModelProviders.of(this, factory) else ViewModelProviders.of(this)

fun androidx.fragment.app.Fragment.viewModelProvider(factory: ViewModelProvider.Factory? = null): ViewModelProvider =
    if (factory != null) ViewModelProviders.of(this, factory) else ViewModelProviders.of(this)

inline fun <reified T : ViewModel> ViewModelProvider.get() = get(T::class.java)