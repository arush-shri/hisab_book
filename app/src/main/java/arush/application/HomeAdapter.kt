package arush.application

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class HomeAdapter(private val dataList: ArrayList<DataModel>, private val listener: RecyclerViewItemClickListener) : RecyclerView.Adapter<HomeAdapter.ViewHolder>(){


    interface RecyclerViewItemClickListener {
        fun onUserClick(userId: String, position: Int)
        fun onAmountClick(oweUserId: String, amount: Float, position: Int)
        fun onCardClick(userId: String, position: Int)

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
        if(currentItem.amount >=0 )
        {
            holder.amount.setTextColor(Color.GREEN)
        }
        else
        {
            holder.amount.setTextColor(Color.RED)
        }
        holder.username.setOnClickListener {
            listener.onUserClick(currentItem.userId,position)
        }
        holder.amount.setOnClickListener {
            listener.onAmountClick(currentItem.userId,currentItem.amount, position)
        }
        holder.card.setOnLongClickListener {
            listener.onCardClick(currentItem.userId, position)
            true
        }
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        val username : TextView = itemView.findViewById(R.id.userNameDisplayer)
        val amount : TextView = itemView.findViewById(R.id.amountDisplayer)
        val card : CardView = itemView.findViewById(R.id.carViewMain)
    }
}