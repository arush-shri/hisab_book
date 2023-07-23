package arush.application

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
            val listType: Type = object : TypeToken<ArrayList<DataModel>>() {}.type
            return gson.fromJson(jsonData, listType)
        }
        return null
    }

    fun setTransactionCount(count: Int)
    {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yy")
        val formatted = current.format(formatter)
        val data = mapOf("Date" to formatted, "Count" to count)

        val file = File(subdir, "transactionCount.json")
        if (file.exists()) {
            val gson = Gson()
            val jsonData = gson.toJson(data)
            val fileWriter = FileWriter(file, false)
            file.writeText(jsonData)
            fileWriter.close()
        }
        else{file.createNewFile()}
    }
    fun getTransactionCount() : Int
    {
        var totalCount = 0
        val file = File(subdir, "transactionCount.json")
        if (file.exists()) {
            val gson = Gson()
            val jsonData = file.readText()
            val data = gson.fromJson(jsonData, Map::class.java)
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yy")
            val formatted = current.format(formatter)

            if(formatted == data["Date"])
            {
                totalCount = (data["Count"] as Double).toInt()
            }
        }
        return totalCount
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
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm a")
        val formatted = current.format(formatter)
        val historyData = HistoryDataModel(lineString, amount, formatted)
        val jsonLine = gson.toJson(historyData)
        file.appendText(jsonLine+"\n")

    }
    fun getHistory(userId: String) : ArrayList<HistoryDataModel>
    {
        val file = File(subdir, "$userId.json")
        val gson = Gson()
        val historyList = ArrayList<HistoryDataModel>()

        if(file.exists())
        {
            val jsonData = file.readLines()
            for(lines in jsonData)
            {
                val data = gson.fromJson(lines, HistoryDataModel::class.java)
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
    fun deleteHistory(currentItem: HistoryDataModel, userId: String)
    {
        val file = File(subdir, "$userId.json")
        val fileLines = file.readLines()
        val gson = Gson()
        for(lines in fileLines)
        {
            val data = gson.fromJson(lines, HistoryDataModel::class.java)
            if(data==currentItem){}
        }
    }
}