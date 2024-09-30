package com.example.myapplication.data

import com.example.myapplication.util.WeatherApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val weatherApi: WeatherApi) {
    suspend fun getCurrentTemperature(latitude: Double, longitude: Double): Double {
        return withContext(Dispatchers.IO) {
            val response = weatherApi.getWeather(
                latitude,
                longitude,
                "temperature_2m,wind_speed_10m"
            )
            response.current.temperature_2m
        }
    }
}