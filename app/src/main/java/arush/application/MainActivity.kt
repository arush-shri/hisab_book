package arush.application

import AccountCreator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import arush.application.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var dbHelper : DBHelper
    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var phoneNum: String
    lateinit var ac: AccountCreator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        dbHelper = DBHelper(applicationContext)

        var userId = intent.getStringExtra("user_id")
        if (userId != null) {
            if (!userId.contains("+91")) {
                userId = "+91$userId"
            }
        }
        var username = intent.getStringExtra("username")

        if (userId != null && username != null) {
            dbHelper.create_user(userId, username, 0.0f)
        }

        mainBinding.addContactButton.tooltipText = "Add contact"
        mainBinding.customAppBar.logoutButton.tooltipText = "Log Out"

        mainBinding.customAppBar.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        ac = AccountCreator(this)
        userId = auth.currentUser?.phoneNumber
        mainBinding.place1Button.text = auth.currentUser?.phoneNumber

        mainBinding.addContactButton.setOnClickListener {
            ac.getContact(this, object : AccountCreator.ContactSelectionListener {
                override fun onContactSelected(phoneNumber: String) {
                    this@MainActivity.phoneNum = phoneNumber
                    if (userId != null) {
                        dbHelper.accountOpener(userId, phoneNum)
                    }
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ac.onActivityResult(requestCode, resultCode, data)
        }

    override fun onDestroy() {
        super.onDestroy()
        dbHelper.terminator()
    }
}
