package com.flux.ui.screens.workspaces

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flux.R
import com.flux.data.model.NotesModel
import com.flux.data.model.WorkspaceModel
import com.flux.data.model.getSpacesList
import com.flux.navigation.NavRoutes
import com.flux.ui.components.DeleteAlert
import com.flux.ui.components.EventToolBar
import com.flux.ui.components.HabitToolBar
import com.flux.ui.components.JournalToolBar
import com.flux.ui.components.NotesToolBar
import com.flux.ui.components.ProgressTrackerToolBar
import com.flux.ui.components.SelectedBar
import com.flux.ui.components.SpacesMenu
import com.flux.ui.components.SpacesToolBar
import com.flux.ui.components.TodoToolBar
import com.flux.ui.events.NotesEvents
import com.flux.ui.events.SettingEvents
import com.flux.ui.state.States

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun LazyListScope.spacesToolbarList(
    navController: NavController,
    isCompactMode: Boolean,
    selectedSpaceId: Int,
    workspaceId: String,
    workspace: WorkspaceModel,
    states: States,
    query: String,
    onSearchNotes: (String) -> Unit,
    onSettingEvents: (SettingEvents) -> Unit,
    onNotesEvents: (NotesEvents) -> Unit,
    onChangeSpace: (Int) -> Unit,
    onEditSpaceList: () -> Unit,
    onAddProgressBarItem: () -> Unit
){
    if(!isCompactMode) item { Spacer(Modifier.height(8.dp)) }
    item {
        val context = LocalContext.current
        val spacesList = getSpacesList()
        val selectedNotes = states.notesState.selectedNotes
        val allNotes = states.notesState.allNotes
        val totalNotes = allNotes.size
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showSpacesMenu by remember { mutableStateOf(false) }
        val importSuccess = stringResource(R.string.import_success)
        val importFailed = stringResource(R.string.import_failed)
        val journalSelectedDate = states.journalState.selectedDate
        val settings = states.settings.data

        val importNoteLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let {
                try {
                    val contentResolver = context.contentResolver
                    val inputStream = contentResolver.openInputStream(uri)
                    val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""

                    // Get filename (remove extension)
                    val fileName = uri.lastPathSegment
                        ?.substringAfterLast("/")
                        ?.substringBeforeLast(".")
                        ?: "Imported Note"

                    // Create a new note
                    val newNote = NotesModel(
                        title = fileName,
                        description = content,
                        workspaceId = workspaceId,
                        lastEdited = System.currentTimeMillis()
                    )

                    onNotesEvents(NotesEvents.UpsertNote(newNote))
                    Toast.makeText(context, importSuccess, Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, importFailed, Toast.LENGTH_SHORT).show()
                }
            }
        }

        if(showDeleteDialog){
            DeleteAlert(onConfirmation = {
                onNotesEvents(NotesEvents.DeleteNotes(allNotes.filter { selectedNotes.contains(it.notesId) }))
                onNotesEvents(NotesEvents.ClearSelection)
                showDeleteDialog=false
            }, onDismissRequest = {
                showDeleteDialog=false
            })
        }

        if (spacesList.find { it.id == selectedSpaceId }?.title == stringResource(R.string.Notes) && selectedNotes.isNotEmpty()) {
            SelectedBar(
                true,
                totalNotes == selectedNotes.size,
                allNotes.filter { selectedNotes.contains(it.notesId) }.all { it.isPinned },
                selectedNotes.size,
                onPinClick = {
                    onNotesEvents(NotesEvents.TogglePinMultiple(allNotes.filter {
                        selectedNotes.contains(
                            it.notesId
                        )
                    }))
                },
                onDeleteClick = { showDeleteDialog = true },
                onSelectAllClick = {
                    if (allNotes.size == selectedNotes.size) {
                        onNotesEvents(NotesEvents.ClearSelection)
                    } else {
                        onNotesEvents(NotesEvents.SelectAllNotes)
                    }
                },
                onCloseClick = { onNotesEvents(NotesEvents.ClearSelection) }
            )
        } else {
            if(!isCompactMode) Spacer(Modifier.height(8.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom=8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpacesToolBar(spacesList.find { it.id == selectedSpaceId }?.title ?: "", spacesList.find { it.id == selectedSpaceId }?.icon
                    ?: Icons.AutoMirrored.Default.Notes,
                    selectedSpaceId == -1,
                    onMainClick = { showSpacesMenu = true },
                    onEditClick = onEditSpaceList
                )
                SpacesMenu(
                    expanded = showSpacesMenu,
                    workspace = workspace,
                    onConfirm = onChangeSpace
                ) { showSpacesMenu = false }

                if (spacesList.find { it.id == selectedSpaceId }?.title == stringResource(R.string.Habits)) {
                    HabitToolBar(context) { navController.navigate(NavRoutes.NewHabit.withArgs(workspaceId, "")) }
                }
                if (spacesList.find { it.id == selectedSpaceId }?.title == stringResource(R.string.Notes)) {
                    NotesToolBar(
                        navController,
                        workspaceId,
                        query,
                        settings.isGridView,
                        onSearch = onSearchNotes,
                        onImportNote = { importNoteLauncher.launch(arrayOf("text/markdown", "text/plain")) },
                        onChangeView = {
                            onSettingEvents(
                                SettingEvents.UpdateSettings(
                                    settings.copy(isGridView = !settings.isGridView)
                                )
                            )
                        })
                }
                if (spacesList.find { it.id == selectedSpaceId }?.title == stringResource(R.string.Journal)) {
                    JournalToolBar(navController, workspaceId, journalSelectedDate,settings.isCalendarMonthlyView) {
                        onSettingEvents(SettingEvents.UpdateSettings(settings.copy(isCalendarMonthlyView = it)))
                    }
                }
                if (spacesList.find { it.id == selectedSpaceId }?.title == stringResource(R.string.To_Do)) {
                    TodoToolBar(navController, workspaceId)
                }
                if (spacesList.find { it.id == selectedSpaceId }?.title == stringResource(R.string.progress_tracker)) {
                    ProgressTrackerToolBar(onAddProgressBarItem)
                }
                if (spacesList.find { it.id == selectedSpaceId }?.title == stringResource(R.string.Events)) {
                    EventToolBar(
                        navController,
                        workspaceId,
                        context,
                        states.eventState.selectedDate,
                        settings.isCalendarMonthlyView,
                        onClick = {
                            onSettingEvents(
                                SettingEvents.UpdateSettings(
                                    settings.copy(isCalendarMonthlyView = it)
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}