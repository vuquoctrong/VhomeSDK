package com.vht.sdkcore.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class ItemOffsetDecoration(
    private val mSpacing: Int,
    private val count: Int = 1
) : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        with(outRect) {
//
//            val position = parent.getChildAdapterPosition(view) + 1
//
//            if (position <= count){
//                top = mSpacing
//            }
//
////            if (position % count == 1){
////                left = mSpacing
////            }
//
//            bottom = mSpacing
//            right = mSpacing

            bottom = mSpacing / 2
            right = mSpacing / 2
            top = mSpacing / 2
            left = mSpacing / 2
        }
    }


}