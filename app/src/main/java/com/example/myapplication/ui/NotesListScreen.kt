package com.example.myapplication.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat.getDrawable
import com.example.myapplication.R
import com.example.myapplication.data.Note
import com.example.myapplication.util.JsonConverter
import com.example.myapplication.util.SortBy
import com.example.myapplication.util.SortOption
import com.example.myapplication.util.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    notes: List<Note>,
    importLauncher: ManagedActivityResultLauncher<Array<String>, Uri?>,
    onAdd: () -> Unit,
    onDelete: (Note) -> Unit,
    onNoteClick: (Note) -> Unit,
    onImport: (List<Note>) -> Unit,
    onExport: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit
) {
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var expandedMenu by remember { mutableStateOf(false) }
    var expandedSortMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current
    Scaffold(
        topBar = {
            Column {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { onSearchQueryChange(it) },
                    label = { Text("Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                TopAppBar(
                    title = { Text("My Notes") },
                    actions = {
                        // Sorting Button
                        IconButton(onClick = { expandedSortMenu = true }) {
                            Icon(ImageVector.vectorResource(R.drawable.filter_list_24px), contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = expandedSortMenu,
                            onDismissRequest = { expandedSortMenu = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                onSortOptionChange(SortOption(SortBy.ID, SortOrder.ASCENDING))
                                expandedSortMenu = false
                            }, text = { Text("ID Ascending") })
                            DropdownMenuItem(onClick = {
                                onSortOptionChange(SortOption(SortBy.ID, SortOrder.DESCENDING))
                                expandedSortMenu = false
                            }, text = { Text("ID Descending") })
                            DropdownMenuItem(onClick = {
                                onSortOptionChange(SortOption(SortBy.TITLE, SortOrder.ASCENDING))
                                expandedSortMenu = false
                            }, text = { Text("Title Ascending") })
                            DropdownMenuItem(onClick = {
                                onSortOptionChange(SortOption(SortBy.TITLE, SortOrder.DESCENDING))
                                expandedSortMenu = false
                            }, text = { Text("Title Descending") })
                            DropdownMenuItem(onClick = {
                                onSortOptionChange(SortOption(SortBy.CONTENT, SortOrder.ASCENDING))
                                expandedSortMenu = false
                            }, text = { Text("Content Ascending") })
                            DropdownMenuItem(onClick = {
                                onSortOptionChange(SortOption(SortBy.CONTENT, SortOrder.DESCENDING))
                                expandedSortMenu = false
                            }, text = { Text("Content Descending") })
                        }

                        // Three-Dotted Options Menu
                        IconButton(onClick = { expandedMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    expandedMenu = false
                                    importLauncher.launch(arrayOf("application/json"))
                                }, text = { Text("Import from JSON") }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    expandedMenu = false
                                    onExport()
                                }, text = { Text("Export to JSON") }
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAdd() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Notes Available. Click + to add one.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(notes) { note ->
                        NoteItem(
                            note = note,
                            onClick = { onNoteClick(note) },
                            onDeleteRequest = { noteToDelete = note }
                        )
                        HorizontalDivider()
                    }
                }
            }
            if (noteToDelete != null) {
                DeleteConfirmationDialog(
                    onConfirm = {
                        onDelete(noteToDelete!!)
                        noteToDelete = null
                    },
                    onDismiss = { noteToDelete = null },
                    message = "Are you sure you want to delete this note?"
                )
            }
        }
    }
}


@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit, message: String) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Note") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NoteItem(note: Note, onClick: () -> Unit, onDeleteRequest: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (note.title.isBlank()) "Untitled" else note.title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = if (note.content.length > 50) "${note.content.take(50)}..." else note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
        IconButton(onClick = { onDeleteRequest() }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Note")
        }
    }
}
