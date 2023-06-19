package arush.application

import android.content.Context
import android.content.pm.PackageManager
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
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

    fun getData() : ArrayList<DataModel>
    {
        val datalist = ArrayList<DataModel>()
        try
        {
            val statement = connection.createStatement()
            val query = "SELECT * FROM owing_table"
            val resultSet : ResultSet = statement.executeQuery(query)
            while (resultSet.next())
            {
                val userId = resultSet.getString("user_id")
                val owes = resultSet.getString("owes")
                val amount = resultSet.getFloat("amount")
                val amnt = resultSet.getFloat("amnt")

                val data = DataModel(userId, owes, amount, amnt)
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

    fun terminator()
    {
        connection.close()
    }
}