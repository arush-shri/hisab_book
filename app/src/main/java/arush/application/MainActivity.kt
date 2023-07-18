package arush.application

import AccountCreator
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
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
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var phoneNum: String
    lateinit var ac: AccountCreator
    private val historyHelper = HistoryHelper(this@MainActivity)
    private lateinit var dataList : ArrayList<DataModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

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

        if(checkConnection() && dbHelper.checkExistence(userId))
        {
            val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
            startActivity(intent)
        }

        var username = intent.getStringExtra("username")
        if (checkConnection() && username != null) {
            dbHelper.createUser(userId, username, 0.0f)
        }

        mainBinding.addContactButton.tooltipText = "Add contact"
        mainBinding.customAppBar.logoutButton.tooltipText = "Log Out"

        mainBinding.customAppBar.logoutButton.setOnClickListener {
            signOut()
        }

        ac = AccountCreator(this)

        mainBinding.addContactButton.setOnClickListener {
            if(checkConnection()){ addContact(userId) }
            else{Toast.makeText(this@MainActivity, "Please connect to the internet", Toast.LENGTH_SHORT).show()}
        }

        mainBinding.recyclerView2.layoutManager = LinearLayoutManager(this)
        mainBinding.recyclerView2.setHasFixedSize(true)
        var tempDataList = historyHelper.retrieveOffline()
        if(tempDataList!=null){dataList=tempDataList}
        dataList = dbHelper.leniData(userId, dataList)
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
                if(!checkConnection()){Toast.makeText(this@MainActivity, "Please connect to the internet", Toast.LENGTH_SHORT).show()}
                else
                {
                    val alert = AlertDialog.Builder(this@MainActivity, R.style.CustomAlertBack)
                    var customAmountView = layoutInflater.inflate(R.layout.custom_alert, null)
                    alert.setView(customAmountView)
                    val alertDialogCustom = alert.create()
                    customAmountView.findViewById<TextView>(R.id.takeAmountButton).setOnClickListener {
                            var amountSetText = customAmountView.findViewById<EditText>(R.id.setAmountText)
                            var amtSet = amountSetText.text.toString()
                            if (!amtSet.isEmpty()) {
                                var amountSet = amtSet.toFloat()
                                dbHelper.insertData(amountSet, userId, dataList[position].userId, 1)
                                historyHelper.setHistory(
                                    dataList[position].userId,
                                    amountSet.toString(),
                                    true
                                )
                                dataList[position].amount = dataList[position].amount + amountSet
                                mainBinding.recyclerView2.adapter?.notifyItemChanged(position)
                                alertDialogCustom.dismiss()
                            }
                            else {
                                Toast.makeText(applicationContext, "Please enter an amount", Toast.LENGTH_SHORT).show()
                            }
                        }

                    customAmountView.findViewById<TextView>(R.id.giveAmountButton).setOnClickListener {
                            var amountSetText = customAmountView.findViewById<EditText>(R.id.setAmountText)
                            var amtSet = amountSetText.text.toString()
                            if (!amtSet.isEmpty()) {
                                var amountSet = amtSet.toFloat()
                                dbHelper.insertData(amtSet.toFloat(), userId, dataList[position].userId, 0)
                                historyHelper.setHistory(dataList[position].userId, amountSet.toString(), true)
                                dataList[position].amount = dataList[position].amount - amountSet
                                mainBinding.recyclerView2.adapter?.notifyItemChanged(position)
                                alertDialogCustom.dismiss()
                            }
                            else {
                                Toast.makeText(applicationContext, "Please enter an amount", Toast.LENGTH_SHORT).show()
                            }
                        }
                    alertDialogCustom.show()
                }
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        historyHelper.storeOffline(dataList)
        if(checkConnection()){ dbHelper.terminator() }
    }

    private fun checkConnection() : Boolean
    {
        val connectManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectManager.activeNetwork ?: return false
        val activeNetwork = connectManager.getNetworkCapabilities(network)
        if (activeNetwork != null) {
            return when{
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        }
        return false
    }

    private fun keepConnecting()
    {
        if(checkConnection())
        {dbHelper = DBHelper(applicationContext)}
        else
        {
            val delay = 1000L
            android.os.Handler().postDelayed({keepConnecting()},delay)
        }
    }
}