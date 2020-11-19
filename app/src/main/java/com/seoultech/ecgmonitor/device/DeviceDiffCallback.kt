package com.seoultech.ecgmonitor.device

import androidx.recyclerview.widget.DiffUtil

class DeviceDiffCallback(private val oldList: List<Device>, private val newList: List<Device>?) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList?.size ?: 0
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList?.get(newItemPosition)
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList?.get(newItemPosition)
    }
}