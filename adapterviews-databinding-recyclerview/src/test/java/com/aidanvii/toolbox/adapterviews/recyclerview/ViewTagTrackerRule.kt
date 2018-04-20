package com.aidanvii.toolbox.adapterviews.recyclerview

import android.view.View
import com.aidanvii.toolbox.databinding.ViewTagTracker
import com.aidanvii.toolbox.databinding.ViewTagTrackerDelegate
import com.aidanvii.toolbox.unchecked
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

@Suppress(unchecked)
class ViewTagTrackerRule : TestRule, ViewTagTrackerDelegate {
    private val dummyViewTags = mutableMapOf<View, MutableMap<Int, Any>>()

    override fun apply(base: Statement, description: Description?) =
        object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                ViewTagTracker.stubDelegate(this@ViewTagTrackerRule)
                try {
                    base.evaluate()
                } finally {
                    ViewTagTracker.unstubDelegate()
                    reset()
                }
            }
        }

    override fun <T> trackInstance(view: View, instanceResourceId: Int, instance: T?): T? {
        return dummyViewTags.getOrPut(view) { mutableMapOf() }.run {
            remove(instanceResourceId).also {
                if (instance != null) {
                    put(instanceResourceId, instance)
                }
            } as T
        }
    }

    override fun <T> getInstance(view: View, instanceResourceId: Int): T? =
        dummyViewTags[view]?.get(instanceResourceId) as T

    fun reset() {
        dummyViewTags.clear()
    }
}