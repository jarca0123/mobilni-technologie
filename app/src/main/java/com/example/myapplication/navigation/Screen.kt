package com.example.myapplication.navigation

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.myapplication.data.Note
import com.example.myapplication.ui.NoteDetailScreen
import com.example.myapplication.ui.NotesListScreen
import com.example.myapplication.util.JsonConverter
import com.example.myapplication.viewmodel.NotesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

sealed class Screen(val route: String) {
    object NotesList : Screen("notes_list")
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Int) = "note_detail/$noteId"
    }
}

@Composable
fun NavGraph(startDestination: String = Screen.NotesList.route, viewModel: NotesViewModel = viewModel()) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val notes by viewModel.notes.collectAsState(initial = emptyList())
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                importNotesFromUri(context, it, viewModel, coroutineScope)
            }
        }
    )

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            uri?.let {
                coroutineScope.launch {
                    exportNotesToUri(context, it, notes)
                }
            }
        }
    )

    NavHost(navController = navController, startDestination = startDestination) {
        composable(route = Screen.NotesList.route) {
            NotesListScreen(
                notes = notes,
                importLauncher = importLauncher,
                onAdd = {
                    viewModel.createNewNote { newNoteId ->
                        navController.navigate(Screen.NoteDetail.createRoute(newNoteId))
                    }
                },
                onDelete = { note ->
                    viewModel.deleteNote(note)
                },
                onNoteClick = { note ->
                    navController.navigate(Screen.NoteDetail.createRoute(note.id))
                },
                onImport = { importedNotes ->
                    viewModel.importNotes(importedNotes)
                },
                onExport = {  ->
                    exportLauncher.launch("notes_export_${System.currentTimeMillis()}.json")
                }
            )
        }

        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: return@composable
            val note = notes.find { it.id == noteId }
            if (note != null) {
                NoteDetailScreen(
                    note = note,
                    onSave = { title, content ->
                        viewModel.updateNote(note.copy(title = title, content = content))
                        navController.popBackStack()
                    },
                    onDelete = {
                        viewModel.deleteNote(note)
                        navController.popBackStack()
                    }
                )
            } else {
                // Handle note not found, possibly navigate back
                navController.popBackStack()
            }
        }
    }
}

private suspend fun exportNotesToUri(context: Context, uri: Uri, notes: List<Note>) {
    val jsonString = JsonConverter.notesToJson(notes)

    try {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            }
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Export successful", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

private fun importNotesFromUri(context: Context, uri: Uri, viewModel: NotesViewModel, coroutineScope: CoroutineScope) {
    coroutineScope.launch {
        val jsonString = readJsonFromUri(context, uri)
        val importedNotes = JsonConverter.jsonToNotes(jsonString)
        viewModel.importNotes(importedNotes)
    }
}

private suspend fun readJsonFromUri(context: Context, uri: Uri): String =
    withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    BufferedReader(reader).use { bufferedReader ->
                        val stringBuilder = StringBuilder()
                        bufferedReader.forEachLine { line ->
                            stringBuilder.append(line)
                        }
                        stringBuilder.toString()
                    }
                }
            } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }