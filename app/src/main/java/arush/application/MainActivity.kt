package arush.application

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import arush.application.databinding.ActivityMainBinding
import java.io.File
import java.io.FileInputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Properties


class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var connection: Connection
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)
        getDBConnection()

        // Connect to ElephantSQL
    }

    fun getDBConnection(){
        Class.forName("com.mysql.jdbc.Driver")
        val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val metaData = applicationInfo.metaData

        val url = metaData.getString("linker")
        val username = metaData.getString("username")
        val password = metaData.getString("chabhi")

        try {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            mainBinding.text.text = "running"
            connection = DriverManager.getConnection(url, username, password)
            val query = connection.prepareStatement("SELECT * FROM temp")
            val result = query.executeQuery()
            while (result.next()) {
                val namedValue = result.getString("named")
                mainBinding.text.text = namedValue
            }

            // Use the connection to execute queries or perform other operations
        } catch (e: SQLException) {
            Log.d("SQLError", e.stackTraceToString())
            mainBinding.text.text = e.message
        }

    }
//    private fun executor(command: String)
//    {
//        try {
//            val statement = connection.createStatement()
//            statement.execute(command)
//        }
//        catch(e: SQLException)
//        {
//            Toast.makeText(this@MainActivity, "Error occured/nRestart the application", Toast.LENGTH_SHORT).show()
//        }
//    }
    override fun onDestroy() {
        super.onDestroy()
        connection.close()
    }


}
