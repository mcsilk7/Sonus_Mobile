package com.example.sonus.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.sonus.MainViewModel
import com.example.sonus.R
import com.example.sonus.ui.home.HomeFragment
import com.example.sonus.ui.library.LibraryFragment
import com.example.sonus.ui.search.SearchFragment
import com.example.sonus.ui.settings.SettingsFragment

class MainContainerFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.viewPager)
        viewPager.adapter = MainPagerAdapter(this)
        
        // Ensure standard bottom nav behavior
        viewPager.isUserInputEnabled = true 

        // Sync ViewPager with ViewModel (Bottom Nav clicks)
        viewModel.currentTabPage.observe(viewLifecycleOwner) { page ->
            if (viewPager.currentItem != page) {
                viewPager.setCurrentItem(page, true)
            }
        }

        // Sync ViewModel with ViewPager (Swipes)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.setTabPage(position)
            }
        })
    }

    private class MainPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment()
                1 -> SearchFragment()
                2 -> LibraryFragment()
                3 -> SettingsFragment()
                else -> HomeFragment()
            }
        }
    }
}
