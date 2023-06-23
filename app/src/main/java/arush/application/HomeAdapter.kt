package arush.application

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class HomeAdapter(private val dataList : ArrayList<DataModel>, private val listener: RecyclerViewItemClickListener) : RecyclerView.Adapter<HomeAdapter.ViewHolder>(){


    interface RecyclerViewItemClickListener {
        fun onItemClick(userId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]
        holder.username.text = currentItem.userId
        holder.amount.text = currentItem.amount.toString()
        holder.itemView.setOnClickListener {
            listener.onItemClick(currentItem.userId)
        }
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        val username : TextView = itemView.findViewById(R.id.userNameDisplayer)
        val amount : TextView = itemView.findViewById(R.id.amountDisplayer)
    }
}