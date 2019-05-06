package com.aidanvii.toolbox.databinding.design

import androidx.databinding.BindingAdapter
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager

@BindingAdapter("viewPager")
internal fun TabLayout.bind(viewPager: ViewPager) = setupWithViewPager(viewPager)