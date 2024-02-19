package com.vht.sdkcore.utils.prefetcher

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

typealias HolderCreator = (fakeParent: ViewGroup, viewType: Int) -> RecyclerView.ViewHolder

interface HolderPrefetch {
    fun setViewsCount(
        viewType: Int,
        count: Int,
        holderCreator: HolderCreator
    )
}

