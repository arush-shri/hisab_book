package arush.application

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val dataList : ArrayList<HistoryDataModel>, private val listener: RecyclerViewItemClickListener) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    interface RecyclerViewItemClickListener{
        fun onCardClick(currentItem: HistoryDataModel, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.history_card, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]
        holder.status.text = currentItem.status
        holder.amount.text = currentItem.amount
        holder.timeDate.text = currentItem.time
        holder.itemView.setOnClickListener {
            listener.onCardClick(currentItem, position)
        }
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        val status : TextView = itemView.findViewById(R.id.sentDisplayer)
        val amount : TextView = itemView.findViewById(R.id.historyAmountDisplayer)
        val timeDate : TextView = itemView.findViewById(R.id.timeDateDisplay)
    }
}