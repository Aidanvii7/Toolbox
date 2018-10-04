package com.aidanvii.toolbox.persistence

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.aidanvii.toolbox.delegates.observable.Functions
import com.aidanvii.toolbox.delegates.observable.ObservableProperty
import kotlin.reflect.KProperty

private val Context.preferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

/**
 * Returns a source [ObservableProperty] ([ObservableProperty.Source.WithProperty]) that persists a boolean value to [SharedPreferences].
 * The [key] is optional, as by default the name of the property ([KProperty.name]) is used.
 * Care must be taken if the [key] is not provided, as changes to the property name across application
 * installs will not use the previous [key], and thus leave stale data in [SharedPreferences].
 *
 * Usage:
 * ```
 * var mySharedPrefBoolean by context.booleanPref(default = false)
 * ```
 *
 * @param ST the base type of the source observable ([ObservableProperty.Source]).
 */
fun Context.booleanPref(
    key: String? = null,
    default: Boolean = false
) = object : ObservableProperty.Source.WithProperty<Boolean>(Functions.areNotEqual) {
    override var sourceValue: Boolean
        get() = preferences.getBoolean(key ?: property.name, default)
        set(value) {
            preferences.edit().apply { putBoolean(key ?: property.name, value); apply() }
        }
}