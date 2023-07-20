package arush.application

import android.content.Context
import android.provider.ContactsContract.Data
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryHelper(private val cont: Context) {

    private val context : Context = cont
    private var subdir : File
    init {
        val dir = context.filesDir

        subdir = File(dir, "Hisab")
        if (!subdir.exists()) {
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
        val fileWriter = FileWriter(file, false)
        fileWriter.write("")
        file.writeText(jsonData)
        fileWriter.close()

    }

    fun retrieveOffline() : ArrayList<DataModel>?
    {
        val file = File(subdir, "OfflineData.json")
        if (file.exists()) {
            val jsonData = file.readText()
            val gson = Gson()
            val listType: Type = object : TypeToken<List<DataModel>>() {}.type
            return gson.fromJson(jsonData, listType)
        }
        return null
    }
    fun accountFileCreator(userId: String)
    {
        val file = File(subdir,"$userId.json")
        if(!file.exists())
        {
            file.createNewFile()
        }
    }
    fun setHistory(userId: String, amount: String, status: Boolean)
    {
        val file = File(subdir, "$userId.json")
        val gson = Gson()
        var lineString = if(status) {
            "Sent"
        } else {
            "Received"
        }
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm")
        val formatted = current.format(formatter)
        val historyData = HistoryDataModel(lineString, amount, formatted)
        val jsonLine = gson.toJson(historyData)
        file.appendText(jsonLine)

    }
    fun getHistory(userId: String) : ArrayList<HistoryDataModel>
    {
        val file = File(subdir, "$userId.json")
        val gson = Gson()
        val historyList = ArrayList<HistoryDataModel>()

        if(file.exists())
        {
            file.readLines().forEach {
                val line = it.trim()
                Log.d("historyIT", line)
                val data = gson.fromJson(line, HistoryDataModel::class.java)
                Log.d("historyJSON", data.toString())
                historyList.add(data)
            }
        }
        return historyList
    }

    fun deleteCompleteHistory(userId: String)
    {
        val file = File(subdir, "$userId.json")
        file.writeText("")
    }
    fun deleteHistory(userId: String)
    {
        val file = File(subdir, "$userId.json")
        val fileLines = file.readLines()
        val gson = Gson()
        for (line in fileLines) {
            val jsonElement = gson.fromJson(line, com.google.gson.JsonElement::class.java)
            val jsonString = gson.toJson(jsonElement)
        }
    }
}