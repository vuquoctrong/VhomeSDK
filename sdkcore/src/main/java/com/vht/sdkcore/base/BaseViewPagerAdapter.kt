package com.vht.sdkcore.base

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

open class BaseViewPagerAdapter(parent: Fragment): FragmentStateAdapter(parent) {

    val fragments = mutableListOf<Fragment>()

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    fun submitList(fragments: List<Fragment>) {
        this.fragments.apply {
            clear()
            addAll(fragments)
        }
        notifyItemRangeChanged(0, fragments.size)
    }

    fun getItem(position: Int) = fragments[position]

}