package com.seoultech.ecgmonitor.bpm.history

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.seoultech.ecgmonitor.R
import com.seoultech.ecgmonitor.databinding.ItemBpmDayPickerBinding
import com.seoultech.ecgmonitor.utils.TimeUtil
import java.util.*

class BPMDayPicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var startTimeInMillis = 0L

    private var dayClickListener: ((selectedCalendar: Calendar) -> Unit)? = null

    init {
        itemAnimator = null
        overScrollMode = OVER_SCROLL_NEVER
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        clipToPadding = false
        setHasFixedSize(true)
        adapter = BPMDayPickerAdapter()
        addItemDecoration(MarginDecoration(10f, HORIZONTAL))
    }

    fun setDayClickListener(listener: (selectedCalendar: Calendar) -> Unit) {
        dayClickListener = listener
    }

    fun setStartTimeInMillis(startTimeInMillis: Long) {
        this.startTimeInMillis = startTimeInMillis
        adapter?.notifyDataSetChanged()

        post {
            layoutManager?.let { layoutManager ->
                adapter?.let { adapter ->
                    (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        adapter.itemCount - 1, 0
                    )
                }
            }
        }
    }

    private inner class BPMDayPickerAdapter :
        Adapter<BPMDayPickerAdapter.BPMDayPickerViewHolder>() {

        private var currentDate = GregorianCalendar()
        private var tempCalendar = GregorianCalendar()
        private var selectedItem = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BPMDayPickerViewHolder {
            selectedItem = itemCount - 1
            return BPMDayPickerViewHolder(parent)
        }

        override fun onBindViewHolder(holder: BPMDayPickerViewHolder, position: Int) {
            holder.bind(position == selectedItem, position)
        }

        override fun getItemCount(): Int {
            return TimeUtil.getDayDiff(currentDate, GregorianCalendar().apply {
                timeInMillis = startTimeInMillis
            }) + 1
        }

        private fun refreshSelectedItem(position: Int) {
            val previousSelectedItem = selectedItem
            selectedItem = position
            notifyItemChanged(previousSelectedItem)
            notifyItemChanged(position)

            dayClickListener?.let { it(getCalendarByPosition(position)) }
        }

        private fun getCalendarByPosition(position: Int): Calendar {
            return tempCalendar.apply {
                timeInMillis = startTimeInMillis
                add(Calendar.DAY_OF_MONTH, position)
            }
        }

        private inner class BPMDayPickerViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_bpm_day_picker, parent, false
            )
        ) {
            private val binding = DataBindingUtil.bind<ItemBpmDayPickerBinding>(itemView)!!
            private val originalTextColor = binding.textviewItembpmdaypickerDay.textColors

            fun bind(isSelected: Boolean, position: Int) {
                val day = getCalendarByPosition(position).get(Calendar.DAY_OF_MONTH)

                binding.run {
                    textviewItembpmdaypickerDay.text = day.toString()

                    if (isSelected) {
                        textviewItembpmdaypickerDay.setTextColor(context.getColor(R.color.white))
                        imageviewItembpmdaypickerCircle.visibility = View.VISIBLE
                    } else {
                        textviewItembpmdaypickerDay.setTextColor(originalTextColor)
                        imageviewItembpmdaypickerCircle.visibility = View.INVISIBLE
                    }
                    root.setOnClickListener {
                        refreshSelectedItem(position)
                    }
                }
            }
        }
    }

    interface BPMDayClickListener {
        fun onDayClick(selectedCalendar: Calendar)
    }
}