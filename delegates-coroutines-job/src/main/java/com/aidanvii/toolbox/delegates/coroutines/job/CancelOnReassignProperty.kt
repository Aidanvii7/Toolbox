package com.aidanvii.toolbox.delegates.coroutines.job

import kotlinx.coroutines.experimental.Job
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Convenience method for creating a [CancelOnReassignProperty]
 */
fun cancelOnReassign(job: Job? = null) = CancelOnReassignProperty(job)

/**
 * A property delegate for [Job] objects that performs cleanup on assignment.
 *
 * When a new [Job] is assigned, [setValue] will call [Job.cancel] on the previous [Job] if non-null.
 * @param job the new [Job]
 */
class CancelOnReassignProperty(@Volatile private var job: Job?) : ReadWriteProperty<Any?, Job?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Job? = job
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Job?) {
        job?.cancel()
        job = value
    }
}