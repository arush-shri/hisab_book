package arush.application

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import arush.application.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var dbHelper : DBHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        dbHelper = DBHelper(applicationContext)
        var userId = intent.getStringExtra("user_id")
        if (userId != null) {
            dbHelper.create_user(userId, "arush", 0.0f)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        dbHelper.terminator()
    }


}
