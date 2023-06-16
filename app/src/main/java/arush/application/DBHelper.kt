package arush.application

import android.content.Context
import android.content.pm.PackageManager
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import java.sql.Connection
import java.sql.DriverManager
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
            val query = connection.prepareStatement("SELECT * FROM temp")
            val result = query.executeQuery()
            while (result.next()) {
                val namedValue = result.getString("named")
            }

            // Use the connection to execute queries or perform other operations
        } catch (e: SQLException) {
            Log.d("SQLError", e.stackTraceToString())
        }
    }

    fun getData() : String?
    {
        val query = connection.prepareStatement("SELECT * FROM temp")
        val result = query.executeQuery()
        var namedValue = "None"
        while (result.next()) {
            namedValue = result.getString("named")
        }
        return namedValue
    }

    fun terminator()
    {
        connection.close()
    }
}