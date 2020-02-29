package com.aidanvii.toolbox.databinding

import android.app.Application
import kotlin.reflect.KProperty
import androidx.databinding.library.baseAdapters.BR

/**
 * Provides a hassle free way of mapping a [KProperty] to an id from a generated `BR` databinding classes.
 *
     * To use, initialise once in your [Application] like so:
 * ```
 * override fun onCreate() {
 *   super.onCreate()
 *   PropertyMapper.initBRClass(BR::class.java)
 * }
 *
 * ```
 */
object PropertyMapper {
    private const val ERROR_NOT_INITIALISED = "PropertyMapper.initBRClass(..) not called"
    private const val ERROR_LOCKED = "PropertyMapper is locked, PropertyMapper.initBRClass(..) can no longer be called"

    private var delegate: PropertyMapperDelegate = UnitialisedPropertyMapper
    private var locked: Boolean = false

    init {
        initBRClass(BR::class.java, locked = false)
    }

    @Synchronized
    fun initBRClass(brClass: Class<*>, locked: Boolean = false) {
        if (this.locked) {
            throw IllegalStateException(ERROR_LOCKED)
        }
        this.locked = locked
        delegate = ClassPropertyMapper(brClass)
    }


    fun getBindableResourceId(property: KProperty<*>) = delegate.getBindableResourceId(property)

    val resourceIds: IntArray get() = delegate.resourceIds

    private interface PropertyMapperDelegate {
        val resourceIds: IntArray
        fun getBindableResourceId(property: KProperty<*>): Int
    }

    object UnitialisedPropertyMapper : PropertyMapperDelegate {
        override val resourceIds: IntArray get() = throw RuntimeException(ERROR_NOT_INITIALISED)

        override fun getBindableResourceId(property: KProperty<*>): Int {
            throw RuntimeException(ERROR_NOT_INITIALISED)
        }
    }

    private class ClassPropertyMapper(brClass: Class<*>) : PropertyMapperDelegate {
        private val propertyIdMap = hashMapOf<String, Int>()

        override val resourceIds = brClass.let {
            it.fields.forEach { field ->
                try {
                    val key = field.name
                    val value = it.getDeclaredField(key).getInt(it)
                    propertyIdMap.put(key, value)
                } catch (e: IllegalArgumentException) {
                    // instant run can inject extra garbage into the BR class, only time I've seen this fail
                }
            }
            propertyIdMap.values.toSortedSet().toIntArray()
        }

        override fun getBindableResourceId(property: KProperty<*>): Int {
            return propertyIdMap[property.name] ?: onPropertyNotFound(property)
        }

        private fun onPropertyNotFound(property: KProperty<*>): Nothing {
            throw RuntimeException("Property not found: ${property.name}.\nExisting mapped properties are: $propertyIdMap")
        }
    }


}
