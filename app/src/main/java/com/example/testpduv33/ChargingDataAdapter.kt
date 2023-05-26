package com.example.testpduv33

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChargingDataAdapter(private val mData:MutableList<MutableMap<String,String>>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.report_list,parent,false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item= holder as ViewHolder
        val map=mData[position]
        item.tvPlan.text=map[GattAttributes.mPlan]
        item.tvStartTime.text=map[GattAttributes.mStartTime]
        item.tvEndTime.text=map[GattAttributes.mEndTime]
        item.tvCurrent.text=map[GattAttributes.mCurrent]
        item.tvVoltage.text=map[GattAttributes.mVoltage]
        item.tvPower.text=map[GattAttributes.mWatt]
        item.tvPowerFactor.text=map[GattAttributes.mPowerFactor]
        item.tvConsumption.text=map[GattAttributes.mConsumption]
        item.tvNone.text=map[GattAttributes.mNone]

    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class ViewHolder(v:View):RecyclerView.ViewHolder(v){
        val tvPlan:TextView=v.findViewById(R.id.tv_row)
        val tvStartTime:TextView=v.findViewById(R.id.tv_start_time)
        val tvEndTime:TextView=v.findViewById(R.id.tv_end_time)
        val tvCurrent:TextView=v.findViewById(R.id.tv_current)
        val tvVoltage:TextView=v.findViewById(R.id.tv_voltage)
        val tvPower:TextView=v.findViewById(R.id.tv_power)
        val tvPowerFactor:TextView=v.findViewById(R.id.tv_power_factor)
        val tvConsumption:TextView=v.findViewById(R.id.tv_consumption)
        val tvNone:TextView=v.findViewById(R.id.tv_none)
    }
}