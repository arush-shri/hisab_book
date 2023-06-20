package arush.application

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import arush.application.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var dbHelper : DBHelper
    val auth : FirebaseAuth = FirebaseAuth.getInstance()
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
        val logout: ImageView = mainBinding.customAppBar.logoutButton
        logout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this@MainActivity,LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        mainBinding.place1Button.text = auth.currentUser?.phoneNumber
    }
    override fun onDestroy() {
        super.onDestroy()
        dbHelper.terminator()
    }


}
