package com.vht.sdkcore.adapter

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemTouchHelperCallback(
    private val onMove: (RecyclerView, RecyclerView.ViewHolder, RecyclerView.ViewHolder) -> Unit,
    private val onSwipe: (RecyclerView.ViewHolder, Int) -> Unit,
    private val onMoveFinish: () -> Unit
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFrag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFrag = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(dragFrag, swipeFrag)
    }

    override fun isLongPressDragEnabled(): Boolean = false

    override fun isItemViewSwipeEnabled(): Boolean = false

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        onMove.invoke(recyclerView, viewHolder, target)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onSwipe.invoke(viewHolder, direction)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        onMoveFinish.invoke()
    }
}