package arush.application

import android.content.Context
import android.content.pm.PackageManager
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import java.lang.Exception
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

class DBHelper (context: Context) {

    private lateinit var connection: Connection
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
            Log.d("SQLError", e.stackTraceToString())
        }
    }

    fun create_user(userId:String, userName: String, debt: Float)
    {
        val statement = connection.createStatement()
        val query = "INSERT INTO users (user_id, user_name, debt) SELECT $userId, '$userName', $debt FROM dual WHERE NOT EXISTS (SELECT 1 FROM users WHERE user_id = $userId)"
        statement.executeUpdate(query)
        statement.close()
    }

    fun accountOpener(user_Id: String, oweId: String): Boolean {

        val statement = connection.createStatement()
        val checkQuery = "SELECT * FROM users WHERE user_id = '$oweId'"
        val checkResult = statement.executeQuery(checkQuery)
        val userId = user_Id.removeRange(0,1)
        if (checkResult.next()) {
            val check_Query = "SELECT * FROM owing_table WHERE (user_id = '$userId' AND owes = '$oweId') OR (user_id = '$oweId' AND owes = '$userId')"
            val check_Result = statement.executeQuery(check_Query)
            if(!check_Result.next())
            {
                val insertQuery ="INSERT INTO owing_table (user_id, owes) VALUES ('$userId', '$oweId')"
                statement.executeUpdate(insertQuery)
                statement.close()
                return true
            }
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
    fun deniData(user_id:String) : ArrayList<DataModel>
    {
        val datalist = ArrayList<DataModel>()
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

    fun insertData(amount: Float, userId: String)
    {
        try {
            val statement = connection.createStatement()
            val query = "UPDATE owing_table SET amount = '$amount' WHERE owes = '$userId'"
            statement.executeUpdate(query)
            statement.close()
        }
        catch (e: Exception){Log.d("Error", e.message.toString())}
    }
    fun terminator()
    {
        connection.close()
    }
}