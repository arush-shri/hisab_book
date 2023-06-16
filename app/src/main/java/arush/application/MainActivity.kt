package arush.application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import arush.application.databinding.ActivityMainBinding
import java.sql.Connection


class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var connection: Connection
    private val dbHelper = DBHelper(this@MainActivity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        val data = dbHelper.getData()
        mainBinding.text.text = data
    }
    override fun onDestroy() {
        super.onDestroy()
        dbHelper.terminator()
    }


}
