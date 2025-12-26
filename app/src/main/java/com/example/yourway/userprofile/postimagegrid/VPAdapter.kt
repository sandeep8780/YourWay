package com.example.yourway.userprofile.postimagegrid

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class VPAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragmentArrayList: ArrayList<Fragment> = ArrayList()
    private val fragmentString: ArrayList<String> = ArrayList()

    override fun createFragment(position: Int): Fragment {
        return fragmentArrayList[position]
    }

    override fun getItemCount(): Int {
        return fragmentArrayList.size
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragmentArrayList.add(fragment)
        fragmentString.add(title)
    }

    fun getPageTitle(position: Int): CharSequence {
        return fragmentString[position]
    }
}