package com.example.schedulingSystem

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val client = OkHttpClient()
    private lateinit var dbHelper: ScheduleDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerSchedules)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        dbHelper = ScheduleDbHelper(this)

        loadSchedule()
    }

    private fun loadSchedule() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // First, try to load from the server
                val request = Request.Builder()
                    .url("http://10.0.2.2/scheduling-api/get-schedule.php")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonData = response.body?.string()
                    val itemType = object : TypeToken<List<ScheduleItem>>() {}.type
                    val data: List<ScheduleItem> = Gson().fromJson(jsonData, itemType)
                    
                    // Save to database
                    dbHelper.insertSchedules(data)

                    // Update UI with new data
                    withContext(Dispatchers.Main) {
                        recyclerView.adapter = ScheduleAdapter(data)
                    }
                } else {
                    throw Exception("Response not successful")
                }

            } catch (e: Exception) {
                Log.e("ERROR", e.message ?: "Unknown error")
                
                // If network fails, load from database
                val localData = dbHelper.getAllSchedules()
                
                withContext(Dispatchers.Main) {
                    if (localData.isNotEmpty()) {
                        recyclerView.adapter = ScheduleAdapter(localData)
                        Toast.makeText(this@MainActivity, "Loaded from local database", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to load data: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
