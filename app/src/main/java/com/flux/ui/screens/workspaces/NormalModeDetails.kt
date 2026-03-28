package com.flux.ui.screens.workspaces

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flux.data.model.WorkspaceModel
import com.flux.data.model.getSpacesList
import com.flux.navigation.NavRoutes
import com.flux.other.icons
import com.flux.ui.components.AddNewSpacesBottomSheet
import com.flux.ui.components.ChangeIconBottomSheet
import com.flux.ui.components.DeleteAlert
import com.flux.ui.components.NewWorkspaceBottomSheet
import com.flux.ui.components.SetPasskeyDialog
import com.flux.ui.components.WorkspaceTopBar
import com.flux.ui.events.HabitEvents
import com.flux.ui.events.JournalEvents
import com.flux.ui.events.NotesEvents
import com.flux.ui.events.TaskEvents
import com.flux.ui.events.TodoEvents
import com.flux.ui.events.WorkspaceEvents
import kotlinx.coroutines.launch
import com.flux.data.model.ProgressBoardModel
import com.flux.other.ensureStorageRoot
import com.flux.ui.components.NewBoardItemSheet
import com.flux.ui.events.ProgressBoardEvents
import com.flux.ui.events.SettingEvents
import com.flux.ui.state.States
import com.flux.ui.viewModel.SettingsViewModel

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceDetails(
    navController: NavController,
    states: States,
    workspace: WorkspaceModel,
    settingsViewModel: SettingsViewModel,
    onWorkspaceEvents: (WorkspaceEvents) -> Unit,
    onNotesEvents: (NotesEvents) -> Unit,
    onTaskEvents: (TaskEvents) -> Unit,
    onHabitEvents: (HabitEvents) -> Unit,
    onTodoEvents: (TodoEvents) -> Unit,
    onJournalEvents: (JournalEvents) -> Unit,
    onSettingEvents: (SettingEvents) -> Unit,
    onProgressBoardEvents: (ProgressBoardEvents) -> Unit
) {
    val context = LocalContext.current
    val workspaceId = workspace.workspaceId
    val isCompactMode = states.settings.data.workspaceGridColumns>1
    val allSpaces = getSpacesList()
    val selectedSpaceId = rememberSaveable { mutableIntStateOf(if (workspace.selectedSpaces.isEmpty()) -1 else workspace.selectedSpaces.first()) }
    val currentSpace = allSpaces.find { it.id == selectedSpaceId.intValue }
    var editWorkspaceDialog by remember { mutableStateOf(false) }
    var editIconSheet by remember { mutableStateOf(false) }
    var showDeleteWorkspaceDialog by remember { mutableStateOf(false) }
    var showLockDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var addSpaceBottomSheet by remember { mutableStateOf(false) }
    var addProgressItem by remember { mutableStateOf(false) }
    var selectedProgressBoardItem by remember { mutableStateOf(ProgressBoardModel(workspaceId=workspaceId)) }
    val spacesList = getSpacesList()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val selectedNotes = states.notesState.selectedNotes
    val allNotes = states.notesState.allNotes
    val expandedTODOIds = rememberSaveable(workspaceId) {
        mutableStateOf<Set<String>>(emptySet())
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> uri?.let { onWorkspaceEvents(WorkspaceEvents.ChangeCover(context, uri, workspace)) } }
    )
    val rootPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val uri = result.data?.data ?: return@rememberLauncherForActivityResult
            settingsViewModel.saveRootUri(uri)
        }

    LaunchedEffect(workspaceId) {
        onNotesEvents(NotesEvents.EnterWorkspace(workspaceId))
        onJournalEvents(JournalEvents.EnterWorkspace(workspaceId))
        onTodoEvents(TodoEvents.EnterWorkspace(workspaceId))
        onTaskEvents(TaskEvents.EnterWorkspace(workspaceId))
        onHabitEvents(HabitEvents.EnterWorkspace(workspaceId))
        onProgressBoardEvents(ProgressBoardEvents.EnterWorkspace(workspaceId))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            WorkspaceTopBar(
                workspace,
                states.settings.data.workspaceGridColumns==1,
                onBackPressed = { navController.popBackStack() },
                onDelete = { showDeleteWorkspaceDialog = true },
                onTogglePinned = {
                    onWorkspaceEvents(
                        WorkspaceEvents.UpsertSpace(
                            workspace.copy(
                                isPinned = !workspace.isPinned
                            )
                        )
                    )
                },
                onToggleLock = {
                    if (workspace.passKey.isNotBlank()) {
                        onWorkspaceEvents(WorkspaceEvents.UpsertSpace(workspace.copy(passKey = "")))
                    } else showLockDialog = true
                },
                onAddCover = { ensureStorageRoot(
                    scope = scope,
                    settingsViewModel = settingsViewModel,
                    rootPicker = rootPicker
                ) { imagePickerLauncher.launch("image/*") } },
                onEditDetails = { editWorkspaceDialog = true },
                onEditLabel = { navController.navigate(NavRoutes.EditLabels.withArgs(workspaceId)) },
                onRemoveCover = { onWorkspaceEvents(WorkspaceEvents.UpsertSpace(workspace.copy(cover = ""))) }
            )
        }
    ) { innerPadding ->
        LazyColumn(Modifier.padding(innerPadding).padding(horizontal = 12.dp).padding(bottom = 8.dp)) {
            if(!isCompactMode){
                item {
                    IconButton(onClick = { editIconSheet = true }) {
                        Icon(
                            icons[workspace.icon],
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                item {
                    Text(
                        workspace.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (workspace.description.isNotBlank()) {
                    item { Text(workspace.description, style = MaterialTheme.typography.bodyLarge) }
                }
            }
            spacesToolbarList(
                navController,
                isCompactMode,
                selectedSpaceId.intValue,
                workspaceId,
                workspace,
                states,
                onSettingEvents,
                onNotesEvents,
                { selectedSpaceId.intValue = it },
                { addSpaceBottomSheet=true },
                {
                    addProgressItem = true
                    selectedProgressBoardItem = ProgressBoardModel(workspaceId=workspaceId)
                }
            )
            spaceDetailItems(
                navController,
                workspaceId,
                workspace.selectedSpaces,
                states,
                query,
                states.settings.data.cornerRadius,
                currentSpace,
                onHabitEvents,
                onNotesEvents,
                onJournalEvents,
                onTaskEvents,
                onTodoEvents,
                expandedTODOIds.value,
                {id -> expandedTODOIds.value = if (id in expandedTODOIds.value) expandedTODOIds.value - id else expandedTODOIds.value + id },
                {
                    addProgressItem=true
                    selectedProgressBoardItem=it
                }
            )
        }
    }

    if (showLockDialog) {
        SetPasskeyDialog(
            { onWorkspaceEvents(WorkspaceEvents.UpsertSpace(workspace.copy(passKey = it))) })
        { showLockDialog = false }
    }

    if (showDeleteWorkspaceDialog) {
        DeleteAlert(onConfirmation = {
            showDeleteWorkspaceDialog = false
            navController.popBackStack()
            onWorkspaceEvents(WorkspaceEvents.DeleteSpace(workspace))
            onNotesEvents(NotesEvents.DeleteAllWorkspaceNotes(workspaceId))
            onTodoEvents(TodoEvents.DeleteAllWorkspaceLists(workspaceId))
            onTaskEvents(TaskEvents.DeleteAllWorkspaceEvents(workspaceId, context))
            onJournalEvents(JournalEvents.DeleteWorkspaceEntries(workspaceId))
            onHabitEvents(HabitEvents.DeleteAllWorkspaceHabits(workspaceId, context))
            onProgressBoardEvents(ProgressBoardEvents.DeleteBoardItemsByWorkspace(workspaceId))
        }, onDismissRequest = {
            showDeleteWorkspaceDialog = false
        })
    }

    AddNewSpacesBottomSheet(
        isVisible = addSpaceBottomSheet,
        sheetState = sheetState,
        selectedSpaces = spacesList.filter { workspace.selectedSpaces.contains(it.id) },
        onDismiss = { addSpaceBottomSheet = false },
        onRemove = { spaceId ->
            val newSelected = workspace.selectedSpaces.firstOrNull { it != spaceId } ?: -1
            selectedSpaceId.intValue = newSelected

            onWorkspaceEvents(
                WorkspaceEvents.UpsertSpace(
                    workspace.copy(selectedSpaces = workspace.selectedSpaces.minus(spaceId))
                )
            )

            removeSpaceData(
                workspaceId, spaceId, context, onTaskEvents, onTodoEvents,
                onHabitEvents, onNotesEvents, onJournalEvents, onProgressBoardEvents
            )
        },
        onSelect = {
            if (selectedSpaceId.intValue == -1) selectedSpaceId.intValue = it
            onWorkspaceEvents(
                WorkspaceEvents.UpsertSpace(
                    workspace.copy(
                        selectedSpaces = workspace.selectedSpaces.plus(
                            it
                        )
                    )
                )
            )
        }
    )

    // Edit Workspace Sheet
    NewWorkspaceBottomSheet(
        isEditing = true,
        workspace = workspace,
        isVisible = editWorkspaceDialog,
        sheetState = sheetState,
        onDismiss = {
            scope.launch { sheetState.hide() }.invokeOnCompletion { editWorkspaceDialog = false }
        },
        onConfirm = {
            onWorkspaceEvents(WorkspaceEvents.UpsertSpace(it))
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                editWorkspaceDialog = false
            }
        }
    )

    // Edit Workspace Sheet
    ChangeIconBottomSheet(
        isVisible = editIconSheet,
        sheetState = sheetState,
        onDismiss = {
            scope.launch { sheetState.hide() }.invokeOnCompletion { editIconSheet = false }
        },
        onConfirm = { index ->
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                onWorkspaceEvents(WorkspaceEvents.UpsertSpace(workspace.copy(icon = index)))
                editIconSheet = false
            }
        }
    )

    NewBoardItemSheet(addProgressItem, sheetState, selectedProgressBoardItem,
        { addProgressItem=false }, {
            addProgressItem=false
            onProgressBoardEvents(ProgressBoardEvents.UpsertProgressItem(it))
            selectedProgressBoardItem=ProgressBoardModel(workspaceId=workspaceId)
        }) {
        onProgressBoardEvents(ProgressBoardEvents.DeleteProgressItem(it))
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
}

fun removeSpaceData(
    workspaceId: String,
    spaceId: Int,
    context: Context,
    onTaskEvents: (TaskEvents) -> Unit,
    onTodoEvents: (TodoEvents) -> Unit,
    onHabitEvents: (HabitEvents) -> Unit,
    onNotesEvents: (NotesEvents) -> Unit,
    onJournalEvents: (JournalEvents) -> Unit,
    onProgressBoardEvents: (ProgressBoardEvents) -> Unit
) {
    when (spaceId) {
        1 -> onNotesEvents(NotesEvents.DeleteAllWorkspaceNotes(workspaceId))
        2 -> onTodoEvents(TodoEvents.DeleteAllWorkspaceLists(workspaceId))
        3 -> onTaskEvents(TaskEvents.DeleteAllWorkspaceEvents(workspaceId, context))
        5 -> onJournalEvents(JournalEvents.DeleteWorkspaceEntries(workspaceId))
        6 -> onHabitEvents(HabitEvents.DeleteAllWorkspaceHabits(workspaceId, context))
        7 -> onProgressBoardEvents(ProgressBoardEvents.DeleteBoardItemsByWorkspace(workspaceId))
    }
}