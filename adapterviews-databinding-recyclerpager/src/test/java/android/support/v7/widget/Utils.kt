package android.support.v7.widget

import android.database.DataSetObservable
import android.support.v4.view.PagerAdapter
import com.nhaarman.mockito_kotlin.mock
import de.jodamob.reflect.SuperReflect

fun PagerAdapter.makeNotifyNotCrash() {
    SuperReflect.on(this).set("mObservable", mock<DataSetObservable>())
}