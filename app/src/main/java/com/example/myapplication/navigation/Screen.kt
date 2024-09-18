package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.NoteDetailScreen
import com.example.myapplication.ui.NotesListScreen
import com.example.myapplication.viewmodel.NotesViewModel

sealed class Screen(val route: String) {
    object NotesList : Screen("notes_list")
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Int) = "note_detail/$noteId"
    }
}

@Composable
fun NavGraph(startDestination: String = Screen.NotesList.route, viewModel: NotesViewModel = viewModel()) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(route = Screen.NotesList.route) {
            NotesListScreen(
                notes = viewModel.notes,
                onAdd = {
                    val newNoteId = viewModel.createNewNote()
                    navController.navigate(Screen.NoteDetail.createRoute(newNoteId))
                },
                onDelete = { id ->
                    viewModel.deleteNote(id)
                },
                onNoteClick = { id ->
                    navController.navigate(Screen.NoteDetail.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: return@composable
            val note = viewModel.getNoteById(noteId)
            if (note != null) {
                NoteDetailScreen(
                    note = note,
                    onSave = { title, content ->
                        viewModel.updateNote(noteId, title, content)
                        navController.popBackStack()
                    },
                    onDelete = {
                        viewModel.deleteNote(noteId)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}