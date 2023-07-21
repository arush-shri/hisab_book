package arush.application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import arush.application.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private lateinit var userId : String
    private lateinit var historyHelper : HistoryHelper
    private lateinit var historyBinding: ActivityHistoryBinding


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
        }
    }
    private fun getHistory()
    {
        val historyArray = historyHelper.getHistory(userId)
        Log.d("historyAct", historyArray.toString())
        historyBinding.historyRecyclerView.adapter = HistoryAdapter(historyArray, object : HistoryAdapter.RecyclerViewItemClickListener
        {
            override fun onCardClick(position: Int) {
                historyHelper.deleteHistory(userId)
                historyArray.removeAt(position)
                historyBinding.historyRecyclerView.adapter?.notifyItemRemoved(position)
            }

        })
    }
}