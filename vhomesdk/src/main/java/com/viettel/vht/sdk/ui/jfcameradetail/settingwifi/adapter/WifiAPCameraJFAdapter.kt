package com.viettel.vht.sdk.ui.jfcameradetail.settingwifi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.viettel.vht.sdk.ui.jfcameradetail.settingwifi.model.WifiAP
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ItemWifiAvailableCameraJfBinding
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible

class WifiAPCameraJFAdapter(
    private val onClickItem: (WifiAP) -> Unit
) : ListAdapter<WifiAP, WifiAPCameraJFAdapter.WifiAPHolder>(WifiAPDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WifiAPHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_wifi_available_camera_jf,
                parent, false
            ),
            onClickItem
        )

    override fun onBindViewHolder(holder: WifiAPHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun submitList(list: List<WifiAP>?) {
        val result = arrayListOf<WifiAP>()
        list?.forEach { result.add(it.copy()) }
        super.submitList(result)
    }

    class WifiAPHolder(
        private val binding: ItemWifiAvailableCameraJfBinding,
        private val onClickItem: (WifiAP) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WifiAP) {
            binding.apply {
                binding.tvNameWifi.text = item.ssid
                when (item.getAuth()) {
                    WifiAP.Auth.OPEN -> {
                        binding.ivLockWifi.gone()
                    }

                    WifiAP.Auth.WPA2PSK -> {
                        binding.ivLockWifi.visible()
                    }
                }
                when (item.getRssi()) {
                    WifiAP.Rssi.Excellent -> {
                        binding.ivStatusWifi.setImageResource(R.drawable.ic_wifi_connection_3)
                    }

                    WifiAP.Rssi.Good, WifiAP.Rssi.VeryGood -> {
                        binding.ivStatusWifi.setImageResource(R.drawable.ic_wifi_connection_2)
                    }

                    WifiAP.Rssi.Low, WifiAP.Rssi.VeryLow -> {
                        binding.ivStatusWifi.setImageResource(R.drawable.ic_wifi_connection_1)
                    }
                }
                root.setOnClickListener {
                    onClickItem(item)
                }
            }
        }
    }
}

class WifiAPDiffCallback : DiffUtil.ItemCallback<WifiAP>() {
    override fun areItemsTheSame(oldItem: WifiAP, newItem: WifiAP): Boolean {
        return oldItem.ssid == newItem.ssid
    }

    override fun areContentsTheSame(oldItem: WifiAP, newItem: WifiAP): Boolean {
        return oldItem == newItem
    }
}
