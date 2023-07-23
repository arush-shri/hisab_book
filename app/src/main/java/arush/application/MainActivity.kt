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
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var dbHelper : DBHelper
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var phoneNum: String
    lateinit var accountCreator: AccountCreator
    private lateinit var historyHelper : HistoryHelper
    private var dataList = ArrayList<DataModel>()
    private lateinit var deletionId : String
    var transactionCount = 0
    var debt = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        historyHelper = HistoryHelper(applicationContext)
        transactionCount = historyHelper.getTransactionCount()

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)

        mainBinding.transactionCountDash.text = transactionCount.toString()

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

        keepConnecting()

        if(checkConnection())
        {
            if(dbHelper.checkExistence(userId))
            {
                val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                startActivity(intent)
            }
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
        mainBinding.transactionCountDash.setOnClickListener {
            Toast.makeText(this@MainActivity, "$transactionCount transactions made today", Toast.LENGTH_SHORT).show()
        }
        mainBinding.debtShowDash.setOnClickListener {
            Toast.makeText(this@MainActivity, "Your total debt is \u20B9$debt", Toast.LENGTH_SHORT).show()
        }
        mainBinding.showHistoryDash.setOnClickListener{
            Toast.makeText(this@MainActivity, "This feature is under development. Please wait for next update :)", Toast.LENGTH_SHORT).show()
        }

        accountCreator = AccountCreator(this)

        mainBinding.addContactButton.setOnClickListener {
            if(checkConnection()){
                addContact(userId)
            }
            else{Toast.makeText(this@MainActivity, "Please connect to the internet", Toast.LENGTH_SHORT).show()}
        }
        deletionId = userId

        mainBinding.recyclerView2.layoutManager = LinearLayoutManager(this)
        mainBinding.recyclerView2.setHasFixedSize(true)
        var tempDataList = historyHelper.retrieveOffline()

        if(tempDataList!=null){dataList=tempDataList}
        if(checkConnection())
        {
            dataList = dbHelper.leniData(userId, dataList)
            dbHelper.deniData(userId, dataList)
        }
        setDebt()
        adapterCreator(userId)

    }

    private fun addContact(userId: String)
    {
        accountCreator.getContact(this, object : AccountCreator.ContactSelectionListener {
            override fun onContactSelected(phoneNumber: String) {
                this@MainActivity.phoneNum = phoneNumber.replace(" ","").replace("+","")
                if (phoneNum.substring(0,2) != "91") {
                    phoneNum = "91$phoneNum"
                }
                var created = dbHelper.accountOpener(userId, phoneNum)
                if(created){
                    var userName = dbHelper.getPhoneUser(phoneNum)
                    dataList.add(0,DataModel(phoneNum,0.0f, userName))
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
        accountCreator.onActivityResult(requestCode, resultCode, data)
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
                                transactionCount++
                                mainBinding.transactionCountDash.text = transactionCount.toString()
                                setDebt()
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
                                historyHelper.setHistory(dataList[position].userId, amountSet.toString(), false)
                                dataList[position].amount = dataList[position].amount - amountSet
                                mainBinding.recyclerView2.adapter?.notifyItemChanged(position)
                                transactionCount++
                                mainBinding.transactionCountDash.text = transactionCount.toString()
                                setDebt()
                                alertDialogCustom.dismiss()
                            }
                            else {
                                Toast.makeText(applicationContext, "Please enter an amount", Toast.LENGTH_SHORT).show()
                            }
                        }
                    alertDialogCustom.show()
                }
            }

            override fun onCardClick(userId: String, currentItem : DataModel, position : Int) {
                deletionProcedure(userId,currentItem, position)
            }
        })
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

    private fun deletionProcedure(userId: String, currentItem : DataModel, position : Int)
    {
        val alert = AlertDialog.Builder(this@MainActivity)
        alert.setMessage("Do you want to delete this contact ?")
        alert.setPositiveButton("Yes") { dialog, _ ->
            historyHelper.deleteCompleteHistory(userId)
            dataList.remove(currentItem)
            mainBinding.recyclerView2.adapter?.notifyItemRemoved(position)
            if(checkConnection())
            {dbHelper.deleter(deletionId, userId)}
            dialog.dismiss()
        }
        alert.setNegativeButton("No"){dialog, _ ->
            dialog.dismiss()
        }
        alert.create().show()
    }

    private fun setDebt()
    {
        var tempDebt = 0.0
        for(data in dataList)
        {

            if(data.amount<0){
                tempDebt += data.amount

            }
            else{
                tempDebt += data.amount
            }
        }
        debt = tempDebt
        if(tempDebt>0)
        {mainBinding.debtTextDash.text = "No debt\nNice :)"}
        else if(tempDebt<0)
        {mainBinding.debtTextDash.text = "You have\nDebt :("}
        else if(tempDebt==0.0)
        {mainBinding.debtTextDash.text = "No debt\nNo Credit ;)"}
        mainBinding.debtShowDash.text = (abs(debt)).toString()
    }
    override fun onStop() {
        super.onStop()
        historyHelper.storeOffline(dataList)
        historyHelper.setTransactionCount(transactionCount)
    }
    override fun onDestroy() {
        super.onDestroy()
        if(checkConnection()){ dbHelper.terminator() }
    }

}