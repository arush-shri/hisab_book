package arush.application

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.widget.Toast
import java.lang.Exception
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

class DBHelper (context: Context) {

    private lateinit var connection: Connection
    private val conte = context
    init {
        Class.forName("com.mysql.jdbc.Driver")
        val applicationInfo = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        val metaData = applicationInfo.metaData

        val url = metaData.getString("linker")
        val username = metaData.getString("username")
        val password = metaData.getString("chabhi")

        try {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            connection = DriverManager.getConnection(url, username, password)

        } catch (e: SQLException) {
            Toast.makeText(context, "Couldn't connect to servers. Please try later ", Toast.LENGTH_SHORT ).show()
        }
    }

    fun checkExistence(userId: String) : Boolean
    {
        if(!checkConnection()){return false}
        else
        {
            val statement = connection.createStatement()
            val query = "SELECT 1 FROM users WHERE user_id = $userId"
            val result = statement.executeQuery(query)
            if (result.next()) {
                statement.close()
                return false
            }
            statement.close()
            return true
        }
    }
    fun create_user(userId:String, userName: String, debt: Float)
    {
        if(checkConnection())
        {
            val statement = connection.createStatement()
            val query =
                "INSERT INTO users (user_id, user_name, debt) SELECT $userId, '$userName', $debt FROM dual WHERE NOT EXISTS (SELECT 1 FROM users WHERE user_id = $userId)"
            statement.executeUpdate(query)
            statement.close()
        }
    }

    fun accountOpener(userId: String, oweId: String): Boolean {

        val statement = connection.createStatement()
        val checkQuery = "SELECT * FROM users WHERE user_id = '$oweId'"
        val checkResult = statement.executeQuery(checkQuery)
        if (checkResult.next()) {
            val check_Query = "SELECT * FROM owing_table WHERE (user_id = '$userId' AND owes = '$oweId') OR (user_id = '$oweId' AND owes = '$userId')"
            val check_Result = statement.executeQuery(check_Query)
            try
            {
            if(!check_Result.next())
            {
                val insertQuery ="INSERT INTO owing_table (user_id, owes) VALUES ('$userId', '$oweId')"
                statement.executeUpdate(insertQuery)
                statement.close()
                return true
            }
            }catch (e: Exception){Log.d("error create",e.message.toString()+userId)}
        }
        statement.close()
        return false
    }

    fun leniData(user_id:String) : ArrayList<DataModel>
    {
        val datalist = ArrayList<DataModel>()
        try
        {
            val statement = connection.createStatement()
            val query = "SELECT * FROM owing_table WHERE user_id = $user_id"
            val resultSet : ResultSet = statement.executeQuery(query)
            while (resultSet.next())
            {
                val owes = resultSet.getString("owes")
                val amount = resultSet.getFloat("amount")

                val data = DataModel(owes, amount)
                datalist.add(data)
            }
            resultSet.close()
            statement.close()
        }
        catch (e:SQLException)
        {
            Log.d("SQLError", e.stackTraceToString())
        }
        return datalist
    }
    fun deniData(user_id:String, dataList: ArrayList<DataModel>)
    {
        try
        {
            val statement = connection.createStatement()
            val query = "SELECT * FROM owing_table WHERE owes = $user_id"
            val resultSet : ResultSet = statement.executeQuery(query)
            while (resultSet.next())
            {
                val userId = resultSet.getString("user_id")
                val amnt = resultSet.getFloat("amnt")

                val data = DataModel(userId, amnt)
                dataList.add(data)
            }
            resultSet.close()
            statement.close()
        }
        catch (e:SQLException)
        {
            Log.d("SQLError", e.stackTraceToString())
        }
    }

    fun insertData(amount: Float, userId: String, oweId: String, commandId: Int)
    {
        val statement = connection.createStatement()
        var checkQuery = "SELECT * FROM owing_table WHERE (user_id = '$userId' AND owes = '$oweId')"
        val executed = statement.executeQuery(checkQuery)
        if(executed.next())
        {
            if(commandId == 1)
            {
                val preAmount = executed.getString("amount").toFloat()
                val preAmnt = executed.getString("amnt").toFloat()
                var query = "UPDATE owing_table SET amount = '${amount+preAmount}', amnt = ${preAmnt-amount} WHERE (user_id = '$userId' AND owes = '$oweId')"
                statement.executeUpdate(query)
            }
            else
            {
                val preAmount = executed.getString("amount").toFloat()
                val preAmnt = executed.getString("amnt").toFloat()
                var query = "UPDATE owing_table SET amount = '${preAmount-amount}', amnt = ${preAmnt+amount} WHERE (user_id = '$userId' AND owes = '$oweId')"
                statement.executeUpdate(query)
            }
        }
        else
        {
            if(commandId == 1)
            {
                val preAmount = executed.getString("amount").toFloat()
                val preAmnt = executed.getString("amnt").toFloat()
                var query = "UPDATE owing_table SET amount = '${preAmount-amount}', amnt = ${preAmnt+amount} WHERE (user_id = '$oweId' AND owes = '$userId')"
                statement.executeUpdate(query)
            }
            else
            {
                val preAmount = executed.getString("amount").toFloat()
                val preAmnt = executed.getString("amnt").toFloat()
                var query = "UPDATE owing_table SET amount = '${preAmount+amount}', amnt = ${preAmnt-amount} WHERE (user_id = '$oweId' AND owes = '$userId')"
                statement.executeUpdate(query)
            }
        }
        statement.close()
    }
    fun terminator()
    {
        connection.close()
    }
    private fun checkConnection() : Boolean
    {
        val connectManager = conte.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
}