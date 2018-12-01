package androidx.appcompat.widget

import android.database.DataSetObservable
import androidx.viewpager.widget.PagerAdapter
import com.nhaarman.mockito_kotlin.mock
import de.jodamob.reflect.SuperReflect

fun PagerAdapter.makeNotifyNotCrash() {
    SuperReflect.on(this).set("mObservable", mock<DataSetObservable>())
}