package arush.application

import android.content.Context
import com.google.gson.Gson
import java.io.File

class HistoryHelper(private val cont: Context) {

    private val context : Context = cont
    private val subdir : File
    init {
        val dir = context.filesDir
        subdir = File(dir, "Hisab")
        if(!subdir.exists())
        {
            subdir.mkdirs()
        }
    }

    fun storeOffline(data: DataModel)
    {
        val file = File(subdir, "OfflineData.json")
        if(!file.exists())
        {
            file.createNewFile()
        }
        val gson = Gson()
        val jsonData = gson.toJson(data)
        file.writeText(jsonData)
    }

    fun retrieveOffline() : DataModel
    {
        val file = File(subdir, "OfflineData.json")
        val jsonData = file.readText()
        val gson = Gson()
        val orgData = gson.fromJson(jsonData, DataModel::class.java)
        return orgData
    }
    fun accountFileCreator(userId: String, data: String)
    {
        val file = File(subdir,"$userId.json")
        val gson = Gson()
        val jsonData = gson.toJson(data)
        file.writeText(jsonData)
    }
    fun fileOpener(fileName: String)
    {}
    fun setHistory()
    {}
    fun getHistory(userId: String)
    {}
}