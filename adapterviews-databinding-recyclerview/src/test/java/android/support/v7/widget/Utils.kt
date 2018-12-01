package androidx.appcompat.widget

import androidx.recyclerview.widget.RecyclerView
import com.nhaarman.mockito_kotlin.mock
import de.jodamob.reflect.SuperReflect

fun androidx.recyclerview.widget.RecyclerView.Adapter<*>.makeNotifyNotCrash() {
    SuperReflect.on(this).set("mObservable", mock<androidx.recyclerview.widget.RecyclerView.AdapterDataObservable>())
}