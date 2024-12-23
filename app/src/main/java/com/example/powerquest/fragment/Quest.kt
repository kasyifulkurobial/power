package com.example.powerquest.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.powerquest.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class Quest : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: androidx.viewpager2.widget.ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quest, container, false)

        // Hubungkan TabLayout dan ViewPager2
        tabLayout = view.findViewById(R.id.tab_layout)
        viewPager = view.findViewById(R.id.view_pager)

        // Atur Adapter ViewPager2
        val adapter = ViewPagerAdapter(requireActivity())
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1

        // Integrasikan TabLayout dengan ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Daily"
                1 -> "Custom"
                else -> null
            }
        }.attach()

        return view
    }

    // Adapter untuk mengelola fragment di ViewPager2
    class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 2 // Jumlah tab (Daily dan Custom)

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DailyFragment()   // Fragment untuk Daily
                1 -> CustomFragment()  // Fragment untuk Custom
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }
}
