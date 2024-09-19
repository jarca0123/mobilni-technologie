package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Note
import com.example.myapplication.data.NoteDatabase
import com.example.myapplication.data.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository

    // Exposed notes as StateFlow for Compose
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    init {
        // Initialize the repository
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)

        // Collect all notes from repository and update _notes
        viewModelScope.launch {
            repository.allNotes.collect { noteList ->
                _notes.value = noteList
            }
        }
    }

    // Create a new empty note and return its ID
    fun createNewNote(onNoteCreated: (Int) -> Unit) {
        viewModelScope.launch {
            val newNote = Note(title = "", content = "")
            val id = repository.insert(newNote).toInt()
            onNoteCreated(id)
        }
    }

    // Update an existing note
    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.update(note)
        }
    }

    // Delete a note
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    // Import notes (replace existing notes)
    fun importNotes(importedNotes: List<Note>) {
        viewModelScope.launch {
            repository.deleteAllNotes()
            importedNotes.forEach { note ->
                repository.insert(note)
            }
        }
    }

    // Export notes to retrieve current list
    fun getAllNotes(): List<Note> {
        return _notes.value
    }
}