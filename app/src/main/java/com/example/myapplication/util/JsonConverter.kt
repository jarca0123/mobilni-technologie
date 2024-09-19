package com.example.myapplication.util

import com.example.myapplication.data.Note
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object JsonConverter {
    private val gson = Gson()

    fun notesToJson(notes: List<Note>): String {
        return gson.toJson(notes)
    }

    fun jsonToNotes(json: String): List<Note> {
        val listType = object : TypeToken<List<Note>>() {}.type
        return gson.fromJson(json, listType)
    }
}