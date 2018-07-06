package com.aidanvii.toolbox.delegates.weak

import kotlinx.coroutines.experimental.Job
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun job(job: Job? = null) = JobProperty(job)

class JobProperty(private var job: Job?) : ReadWriteProperty<Any?, Job?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Job? = job
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Job?) {
        job?.cancel()
        job = value
    }
}