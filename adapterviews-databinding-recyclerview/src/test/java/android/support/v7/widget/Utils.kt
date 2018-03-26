package android.support.v7.widget

import android.support.v7.widget.RecyclerView
import com.nhaarman.mockito_kotlin.mock
import de.jodamob.reflect.SuperReflect

fun RecyclerView.Adapter<*>.makeNotifyNotCrash() {
    SuperReflect.on(this).set("mObservable", mock<RecyclerView.AdapterDataObservable>())
}