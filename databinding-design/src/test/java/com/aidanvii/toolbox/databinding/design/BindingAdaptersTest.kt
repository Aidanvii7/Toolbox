package com.aidanvii.toolbox.databinding.design

import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BindingAdaptersTest {

    val viewPager = mock<ViewPager>()
    val tabLayout = mock<TabLayout>()

    @Nested
    inner class `When bind is called` {

        @BeforeEach
        fun givenWhen() = tabLayout.bind(viewPager)

        @Test
        fun `calls setupWithViewPager`() = verify(tabLayout).setupWithViewPager(viewPager)
    }
}