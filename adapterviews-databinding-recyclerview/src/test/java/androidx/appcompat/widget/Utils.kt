package androidx.appcompat.widget

import androidx.recyclerview.widget.RecyclerView
import com.nhaarman.mockito_kotlin.mock
import de.jodamob.reflect.SuperReflect


fun RecyclerView.Adapter<*>.makeNotifyNotCrash() {
    // TODO see Github issue #7
//    SuperReflect.on(this).set("mObservable", mock<RecyclerView.AdapterDataObservable>())
}