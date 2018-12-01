package com.aidanvii.toolbox

import android.content.Context
import androidx.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton

operator fun ViewGroup.plusAssign(view: View) = addView(view)
operator fun ViewGroup.minusAssign(view: View) = removeView(view)

val View.parentViewGroup get() = this.parent as? ViewGroup

val ViewGroup.children get() = (0 until childCount).map { getChildAt(it) }

val Context.layoutInflater: LayoutInflater get() = LayoutInflater.from(this)

val View.layoutInflater: LayoutInflater get() = context.layoutInflater

val View.parentView: View? get() = parent as? View

val View.marginLayoutParams: ViewGroup.MarginLayoutParams?
    get() = layoutParams as? ViewGroup.MarginLayoutParams

fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
    marginLayoutParams?.setMargins(left, top, right, bottom)
}

fun ViewGroup.inflateAndAttach(@LayoutRes resource: Int): View? = LayoutInflater.from(context).inflate(resource, this, true)