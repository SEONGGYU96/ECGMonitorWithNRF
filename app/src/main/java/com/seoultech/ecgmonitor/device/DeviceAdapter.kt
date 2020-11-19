package com.seoultech.ecgmonitor.device

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.databinding.ItemDeviceBinding

class DeviceAdapter(private val context: Context, deviceLiveData: DeviceLiveData) :
    RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {
    private var devices = mutableListOf<Device>()
    var listener: ((BluetoothDevice) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.item_device, parent, false), listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    override fun getItemId(position: Int): Long {
        return devices[position].hashCode().toLong()
    }

    init {
        setHasStableIds(true)
        deviceLiveData.observe(context as LifecycleOwner) {
            val result = DiffUtil.calculateDiff(
                DeviceDiffCallback(devices, it), false)
            devices.clear()
            if (it != null) {
                devices.addAll(it)
            }
            result.dispatchUpdatesTo(this)
        }
    }

    class ViewHolder(view: View, private val listener: ((BluetoothDevice) -> Unit)?) : RecyclerView.ViewHolder(view) {
        private val binding = DataBindingUtil.bind<ItemDeviceBinding>(itemView)!!
        fun bind(data: Device) {
            binding.run {
                constraintlayoutItemdevice.setOnClickListener {
                    if (data.device != null && listener != null) {
                        listener!!(data.device!!)
                    }
                }
                device = data
            }
        }
    }
}