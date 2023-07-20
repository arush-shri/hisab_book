package arush.application

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val dataList : ArrayList<HistoryDataModel>, private val listener: RecyclerViewItemClickListener) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    interface RecyclerViewItemClickListener{
        fun onCardClick(position: Int)
    }
    init {
        Log.d("adapter", dataList.toString())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("adapterC", dataList.toString())
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.history_card, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        Log.d("adapterI", dataList.size.toString())
        return dataList.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("adapterB", dataList.toString())
        val currentItem = dataList[position]
        holder.status.text = currentItem.status
        holder.amount.text = currentItem.amount
        holder.timeDate.text = currentItem.time
        holder.itemView.setOnClickListener {
            listener.onCardClick(position)
        }
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        init {
            Log.d("adapterVH", "HERE")
        }
        val status : TextView = itemView.findViewById(R.id.sentDisplayer)
        val amount : TextView = itemView.findViewById(R.id.historyAmountDisplayer)
        val timeDate : TextView = itemView.findViewById(R.id.timeDateDisplay)
    }
}