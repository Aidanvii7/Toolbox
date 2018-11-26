package com.aidanvii.toolbox.databinding.design

import android.databinding.BindingAdapter
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager

@BindingAdapter("android:viewPager")
internal fun TabLayout.bind(viewPager: ViewPager) = setupWithViewPager(viewPager)