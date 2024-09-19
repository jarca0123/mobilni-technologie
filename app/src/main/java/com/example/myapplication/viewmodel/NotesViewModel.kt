package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Note
import com.example.myapplication.data.NoteDatabase
import com.example.myapplication.data.NoteRepository
import com.example.myapplication.util.SortBy
import com.example.myapplication.util.SortOption
import com.example.myapplication.util.SortOrder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository

    // Exposed notes as StateFlow for Compose
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    // StateFlow for search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // StateFlow for sorting options
    private val _sortOption = MutableStateFlow(SortOption())
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // Combine notes with search and sort states
    var filteredAndSortedNotes: StateFlow<List<Note>>? = null

        init {
        // Initialize the repository
        val noteDao = NoteDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        filteredAndSortedNotes = combine(
            repository.allNotes,
            _searchQuery,
            _sortOption
        ) { notes, query, sortOption: SortOption ->
            // Apply search filter
            val filteredNotes = if (query.isEmpty()) {
                notes
            } else {
                notes.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.content.contains(query, ignoreCase = true)
                }
            }

            // Apply sorting
            val sortedNotes = when (sortOption.sortBy) {
                SortBy.ID -> {
                    when (sortOption.sortOrder) {
                        SortOrder.ASCENDING -> filteredNotes.sortedBy { it.id }
                        SortOrder.DESCENDING -> filteredNotes.sortedByDescending { it.id }
                    }
                }
                SortBy.TITLE -> {
                    when (sortOption.sortOrder) {
                        SortOrder.ASCENDING -> filteredNotes.sortedBy { it.title.lowercase() }
                        SortOrder.DESCENDING -> filteredNotes.sortedByDescending { it.title.lowercase() }
                    }
                }
                SortBy.CONTENT -> {
                    when (sortOption.sortOrder) {
                        SortOrder.ASCENDING -> filteredNotes.sortedBy { it.content.lowercase() }
                        SortOrder.DESCENDING -> filteredNotes.sortedByDescending { it.content.lowercase() }
                    }
                }
            }

            sortedNotes
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )
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

    // Update search query
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Update sorting option
    fun updateSortOption(sortOption: SortOption) {
        _sortOption.value = sortOption
    }
}