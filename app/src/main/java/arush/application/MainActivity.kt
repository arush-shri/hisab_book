package arush.application

import AccountCreator
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import arush.application.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var dbHelper : DBHelper
    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var phoneNum: String
    lateinit var ac: AccountCreator
    lateinit var dataList : ArrayList<DataModel>
    private val historyHelper = HistoryHelper(this@MainActivity)

    override fun onStart() {
        super.onStart()
        dataList = historyHelper.retrieveOffline()
    }
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
            signOut()
        }

        ac = AccountCreator(this)

        mainBinding.addContactButton.setOnClickListener {
            addContact(userId)
        }

        mainBinding.recyclerView2.layoutManager = LinearLayoutManager(this)
        mainBinding.recyclerView2.setHasFixedSize(true)
        dataList = dbHelper.leniData(userId)
        dbHelper.deniData(userId, dataList)

        adapterCreator(userId)

    }

    private fun addContact(userId: String)
    {
        ac.getContact(this, object : AccountCreator.ContactSelectionListener {
            override fun onContactSelected(phoneNumber: String) {
                this@MainActivity.phoneNum = phoneNumber
                var created = dbHelper.accountOpener(userId, phoneNum)
                if(created){
                    dataList.add(0,DataModel(phoneNum,0.0f))
                    mainBinding.recyclerView2.adapter?.notifyItemInserted(0)
                    historyHelper.accountFileCreator(phoneNum)
                }
                else
                {
                    Toast.makeText(applicationContext, "User does not exist on hisab book", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private fun signOut()
    {
        auth.signOut()
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ac.onActivityResult(requestCode, resultCode, data)
        }

    private fun adapterCreator(userId: String)
    {
        mainBinding.recyclerView2.adapter = HomeAdapter(dataList, object : HomeAdapter.RecyclerViewItemClickListener {

            override fun onUserClick(userId: String, position: Int) {
                val intent = Intent(this@MainActivity, HistoryActivity::class.java)
                intent.putExtra("user_id", userId)
                startActivity(intent)
            }

            override fun onAmountClick(oweUserId: String, amount: Float, position: Int) {

                val alert = AlertDialog.Builder(this@MainActivity,R.style.CustomAlertBack)

                var customAmountView = layoutInflater.inflate(R.layout.custom_alert, null)
                alert.setView(customAmountView)
                val alertDialogCustom = alert.create()

                customAmountView.findViewById<TextView>(R.id.takeAmountButton).setOnClickListener {
                    var amountSetText = customAmountView.findViewById<EditText>(R.id.setAmountText)
                    var amtSet = amountSetText.text.toString()
                    if(!amtSet.isEmpty()){
                        var amountSet = amtSet.toFloat()
                        dbHelper.insertData(amountSet, userId, dataList[position].userId, 1)
                        dataList[position].amount = dataList[position].amount + amountSet
                        mainBinding.recyclerView2.adapter?.notifyItemChanged(position)
                        alertDialogCustom.dismiss()
                    }
                    else{
                        Toast.makeText(applicationContext,"Please enter an amount",Toast.LENGTH_SHORT).show()
                    }
                }

                customAmountView.findViewById<TextView>(R.id.giveAmountButton).setOnClickListener {
                    var amountSetText = customAmountView.findViewById<EditText>(R.id.setAmountText)
                    var amtSet = amountSetText.text.toString()
                    if(!amtSet.isEmpty()){
                        var amountSet = amtSet.toFloat()
                        dbHelper.insertData(amtSet.toFloat(), userId, dataList[position].userId, 0)
                        dataList[position].amount = dataList[position].amount - amountSet
                        mainBinding.recyclerView2.adapter?.notifyItemChanged(position)
                        alertDialogCustom.dismiss()
                    }
                    else{
                        Toast.makeText(applicationContext,"Please enter an amount",Toast.LENGTH_SHORT).show()
                    }
                }
                alertDialogCustom.show()
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        historyHelper.storeOffline(dataList)
        dbHelper.terminator()
    }
}