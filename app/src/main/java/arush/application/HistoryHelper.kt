package arush.application

import android.content.Context
import android.provider.ContactsContract.Data
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.io.File
import java.lang.reflect.Type

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

    fun storeOffline(data: ArrayList<DataModel>)
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

    fun retrieveOffline() : ArrayList<DataModel>
    {
        val file = File(subdir, "OfflineData.json")
        val jsonData = file.readText()
        val gson = Gson()
        val listType: Type = object : TypeToken<List<DataModel>>() {}.type
        return gson.fromJson(jsonData, listType)
    }
    fun accountFileCreator(userId: String)
    {
        val file = File(subdir,"$userId.json")
        if(!file.exists())
        {
            file.createNewFile()
        }
    }
    fun setHistory()
    {}
    fun getHistory(userId: String) : ArrayList<String>
    {
        val file = File(subdir, "$userId.json")
        val gson = Gson()
        val stringList = ArrayList<String>()
        val fileLines = file.readLines()
        for (line in fileLines) {
            val jsonElement = gson.fromJson(line, com.google.gson.JsonElement::class.java)
            val jsonString = gson.toJson(jsonElement)
            stringList.add(jsonString)
        }
        return stringList
    }
}