package com.aidanvii.toolbox.databinding.design

import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BindingAdaptersTest {

    val viewPager = mock<androidx.viewpager.widget.ViewPager>()
    val tabLayout = mock<TabLayout>()

    @Nested
    inner class `When bind is called` {

        @BeforeEach
        fun givenWhen() = tabLayout.bind(viewPager)

        @Test
        fun `calls setupWithViewPager`() = verify(tabLayout).setupWithViewPager(viewPager)
    }
}