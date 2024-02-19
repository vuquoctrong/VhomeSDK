package com.vht.sdkcore.utils.widget

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.vht.sdkcore.R
import com.vht.sdkcore.utils.dialog.ItemRadio

class RadioAdapter<T : ItemRadio>(
    private val onRadioClick: ((T) -> Unit)? = null,
    @LayoutRes private val itemLayoutId: Int = R.layout.item_radio
) : RecyclerView.Adapter<RadioAdapter.RadioViewHolder>() {

    class RadioViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvTitle: TextView = binding.root.findViewById(R.id.tvTitle)
        val ivRadio: ImageView = binding.root.findViewById(R.id.ivRadio)
        val container: View = binding.root.findViewById(R.id.container)
    }

    private val radios = mutableListOf<T>()

    private var selectedItem: T? = null

    @DrawableRes
    private var iconUnselectedRadio: Int = R.drawable.ic_radio_unselected

    @DrawableRes
    private var iconSelectedRadio: Int = R.drawable.ic_radio_selected

    @SuppressLint("NotifyDataSetChanged")
    fun assignAll(data: MutableList<T>) {
        radios.clear()
        radios.addAll(data)
        radios.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItem(selected: T?) {
        selectedItem = selected
        if (selected == null) {
            radios.forEach { it.isSelected = false }
        } else {
            radios.forEach { it.isSelected = it.getItemId() == selected.getItemId() }
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeIconRadioSelected(@DrawableRes drawableRes: Int) {
        iconSelectedRadio = drawableRes
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeIconRadioUnselected(@DrawableRes drawableRes: Int) {
        iconUnselectedRadio = drawableRes
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearData() {
        radios.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioViewHolder {
        val binding: ViewDataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), itemLayoutId, parent, false
        )
        return RadioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RadioViewHolder, position: Int) {
        val radio = radios[position]
        holder.tvTitle.text = radio.getText()
        holder.ivRadio.setImageResource(if (selectedItem?.getItemId() == radio.getItemId()) iconSelectedRadio else iconUnselectedRadio)
        holder.container.setOnClickListener {
            if (!radio.isSelected) {
                unSelectedAll()
                radio.isSelected = true
                selectedItem = radio
                holder.ivRadio.setImageResource(iconSelectedRadio)
                onRadioClick?.invoke(radio)
            }
        }
    }

    fun getSelectedItem() = selectedItem

    @SuppressLint("NotifyDataSetChanged")
    private fun unSelectedAll() {
        radios.forEach { if (it.isSelected) it.isSelected = false }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount() = radios.size
}
