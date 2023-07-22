package arush.application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import arush.application.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var userId : String
    private lateinit var historyHelper : HistoryHelper
    private lateinit var historyBinding: ActivityHistoryBinding
    private var arraySize =  0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        historyBinding = ActivityHistoryBinding.inflate(layoutInflater)
        val view = historyBinding.root
        setContentView(view)

        historyHelper  = HistoryHelper(applicationContext)
        userId = intent.getStringExtra("user_id").toString()

        getHistory()

        historyBinding.deleteHistoryButton.setOnClickListener{
            historyHelper.deleteCompleteHistory(userId)
            historyBinding.historyRecyclerView.adapter?.notifyItemRangeRemoved(0, arraySize-1)
        }
    }
    private fun getHistory()
    {
        val historyArray = historyHelper.getHistory(userId)
        arraySize = historyArray.size
        Log.d("historyAct", historyArray.toString())
        historyBinding.historyRecyclerView.adapter = HistoryAdapter(historyArray, object : HistoryAdapter.RecyclerViewItemClickListener
        {
            override fun onCardClick(currentItem: HistoryDataModel, position: Int) {
//                historyHelper.deleteHistory(currentItem, userId)
//                historyArray.remove(currentItem)
//                historyBinding.historyRecyclerView.adapter?.notifyItemRemoved(position)
                if(currentItem.status=="Sent")
                {Toast.makeText(this@HistoryActivity, "Ask them to return your money", Toast.LENGTH_SHORT).show()}
                else
                {Toast.makeText(this@HistoryActivity, "Please return their money", Toast.LENGTH_SHORT).show()}
            }

        })
    }
}