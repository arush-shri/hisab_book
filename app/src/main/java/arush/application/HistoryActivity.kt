package arush.application

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class HistoryActivity : AppCompatActivity() {

    private val userId= intent.getStringExtra("user_id")
    private val historyHelper : HistoryHelper = HistoryHelper(this@HistoryActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

    }
}