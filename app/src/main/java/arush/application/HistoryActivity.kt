package arush.application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import arush.application.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {

    private val userId = intent.getStringExtra("user_id").toString()
    private val historyHelper : HistoryHelper = HistoryHelper(this@HistoryActivity)
    private lateinit var historyBinding: ActivityHistoryBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        historyBinding = ActivityHistoryBinding.inflate(layoutInflater)
        val view = historyBinding.root
        setContentView(view)

        getHistory()

        historyBinding.deleteHistoryButton.setOnClickListener{
            historyHelper.deleteCompleteHistory(userId)
        }
    }
    private fun getHistory()
    {
        val historyArray = historyHelper.getHistory(userId)
        historyBinding.historyRecyclerView.adapter = HistoryAdapter(userId,historyArray, object : HistoryAdapter.RecyclerViewItemClickListener
        {
            override fun onCardClick(position: Int) {
                historyHelper.deleteHistory(userId)
                historyArray.removeAt(position)
                historyBinding.historyRecyclerView.adapter?.notifyItemRemoved(position)
            }

        })
    }
}