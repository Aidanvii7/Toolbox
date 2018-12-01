package androidx.appcompat.widget

import androidx.recyclerview.widget.RecyclerView
import com.nhaarman.mockito_kotlin.mock
import de.jodamob.reflect.SuperReflect

fun RecyclerView.Adapter<*>.makeNotifyNotCrash() {
//    SuperReflect.on(this).set("mObservable", mock<RecyclerView.AdapterDataObservable>())
}