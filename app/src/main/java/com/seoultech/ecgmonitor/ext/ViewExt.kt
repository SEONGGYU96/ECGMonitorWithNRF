package com.seoultech.ecgmonitor

import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.seoultech.ecgmonitor.base.BaseAdapter

@BindingAdapter("replaceAll")
fun RecyclerView.replaceAll(list: List<Nothing>?) {
    if (adapter != null) {
        (adapter as BaseAdapter<*>).run {
            if (list != null) {
                setList(list)
            } else {
                clear()
            }
        }
    }
}

@BindingAdapter("searchedCountText")
fun AppCompatTextView.setSearchedCountText(count: Int) {
    text = String.format(context.getString(R.string.contactsearch_result_count), count)
}