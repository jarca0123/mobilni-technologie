package com.example.myapplication.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Note

class NotesViewModel : ViewModel() {
    private val _notes = mutableStateListOf<Note>()
    val notes: List<Note> get() = _notes
    private var nextId = 1

    fun addNote(title: String, content: String) {
        _notes.add(Note(id = nextId++, title = title, content = content))
    }

    fun updateNote(id: Int, title: String, content: String) {
        val index = _notes.indexOfFirst { it.id == id }
        if (index != -1) {
            _notes[index] = _notes[index].copy(title = title, content = content)
        }
    }

    fun deleteNote(id: Int) {
        _notes.removeAll { it.id == id }
    }

    fun getNoteById(id: Int): Note? {
        return _notes.find { it.id == id }
    }
}