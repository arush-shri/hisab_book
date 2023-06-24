package arush.application

import AccountCreator
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import arush.application.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var dbHelper : DBHelper
    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var phoneNum: String
    lateinit var ac: AccountCreator
    lateinit var dataList : ArrayList<DataModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        dbHelper = DBHelper(applicationContext)

        var userId = intent.getStringExtra("user_id")
        if (userId == null) {
            userId = auth.currentUser?.phoneNumber.toString()
        }
        if (!userId.contains("+91")) {
            userId = "91$userId"
        }
        else if(userId.contains("+91"))
        {
            userId = userId.removeRange(0,1)
        }

        if(dbHelper.checkExistence(userId))
        {
            val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
            startActivity(intent)
        }

        var username = intent.getStringExtra("username")
        if (username != null) {
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
        mainBinding.place1Button.text = auth.currentUser?.phoneNumber

        mainBinding.addContactButton.setOnClickListener {
            ac.getContact(this, object : AccountCreator.ContactSelectionListener {
                override fun onContactSelected(phoneNumber: String) {
                    this@MainActivity.phoneNum = phoneNumber
                    if (userId != null) {
                        var created = dbHelper.accountOpener(userId, phoneNum)
                        if(created){
                            dataList.add(0,DataModel(phoneNum,0.0f))
                            mainBinding.recyclerView2.adapter?.notifyItemInserted(0)
                        }
                        else
                        {
                            Toast.makeText(applicationContext, "User does not exist on hisab book", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

        mainBinding.recyclerView2.layoutManager = LinearLayoutManager(this)
        mainBinding.recyclerView2.setHasFixedSize(true)
        dataList = dbHelper.leniData(userId)
        dbHelper.deniData(userId, dataList)

        mainBinding.recyclerView2.adapter = HomeAdapter(dataList, object : HomeAdapter.RecyclerViewItemClickListener {

            override fun onUserClick(userId: String, position: Int) {
                Toast.makeText(applicationContext, userId, Toast.LENGTH_SHORT).show()
            }

            override fun onAmountClick(oweUserId: String, amount: Float, position: Int) {

                val alert = AlertDialog.Builder(this@MainActivity,R.style.CustomAlertBack)

                var customAmountView = layoutInflater.inflate(R.layout.custom_alert, null)
                alert.setView(customAmountView)
                val alertDialogCustom = alert.create()

                customAmountView.findViewById<TextView>(R.id.takeAmountButton).setOnClickListener {
                    var amountSetText = customAmountView.findViewById<EditText>(R.id.setAmountText)
                    var amountSet = amountSetText.text.toString().toFloat()
                    dbHelper.insertData(amountSet, userId, dataList[position].userId, 1)
                    dataList[position].amount = dataList[position].amount+amountSet
                    mainBinding.recyclerView2.adapter?.notifyItemChanged(position)
                    alertDialogCustom.dismiss()
                }

                customAmountView.findViewById<TextView>(R.id.giveAmountButton).setOnClickListener {
                    var amountSetText = customAmountView.findViewById<EditText>(R.id.setAmountText)
                    var amountSet = amountSetText.text.toString().toFloat()
                    dbHelper.insertData(amountSet, userId, dataList[position].userId, 0)
                    dataList[position].amount = dataList[position].amount-amountSet
                    mainBinding.recyclerView2.adapter?.notifyItemChanged(position)
                    alertDialogCustom.dismiss()
                }
                alertDialogCustom.show()
            }
        })

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
