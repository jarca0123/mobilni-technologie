package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Note
import com.example.myapplication.util.WeatherApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    note: Note,
    onSave: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Edit Note") }) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Button(
                onClick = {
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://api.open-meteo.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val weatherApi = retrofit.create(WeatherApi::class.java)

                    // Insert code here
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = weatherApi.getWeather(
                                latitude = 52.52,
                                longitude = 13.41,
                                current = "temperature_2m,wind_speed_10m"
                            )
                            val temperature = response.current.temperature_2m

                            withContext(Dispatchers.Main) {
                                // Update your UI state here
                                // For example, if you're using a MutableState:
                                content += temperature.toString()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                // Handle error, update UI to show error message
                                // For example:
                                content += "Failed to fetch temperature: ${e.message}"
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Získat teplotu")
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = Int.MAX_VALUE
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        onSave(title, content)
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save")
            }
        }
    }
}