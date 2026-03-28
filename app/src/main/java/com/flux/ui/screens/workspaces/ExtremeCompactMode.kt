package com.flux.ui.screens.workspaces

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flux.R
import com.flux.data.model.WorkspaceModel
import com.flux.data.model.getSpacesList
import com.flux.navigation.NavRoutes
import com.flux.ui.components.AddNewSpacesBottomSheet
import com.flux.ui.components.ChangeIconBottomSheet
import com.flux.ui.components.DeleteAlert
import com.flux.ui.components.NewWorkspaceBottomSheet
import com.flux.ui.components.SetPasskeyDialog
import com.flux.ui.events.HabitEvents
import com.flux.ui.events.JournalEvents
import com.flux.ui.events.NotesEvents
import com.flux.ui.events.TaskEvents
import com.flux.ui.events.TodoEvents
import com.flux.ui.events.WorkspaceEvents
import kotlinx.coroutines.launch
import com.flux.data.model.ProgressBoardModel
import com.flux.other.icons
import com.flux.ui.components.ChangeWorkspaceDialog
import com.flux.ui.components.EmptySpaces
import com.flux.ui.components.ExtremeCompactModeDropDown
import com.flux.ui.components.NewBoardItemSheet
import com.flux.ui.events.ProgressBoardEvents
import com.flux.ui.state.States
import com.flux.ui.viewModel.ViewModels

@Composable
fun CompactWorkspaceCard(icon: Int, isLocked: Boolean, title: String, onClick: ()->Unit){
    Card(
        modifier = Modifier.clip(RoundedCornerShape(50)),
        shape = RoundedCornerShape(50),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            if(!isLocked){
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) { Icon(icons[icon], null, Modifier.size(18.dp)) }
            }
            else{
                Row(Modifier.padding(end = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomStart = 32.dp, topStart = 32.dp))
                            .clip(RoundedCornerShape(bottomStart = 32.dp, topStart = 32.dp))
                            .padding(7.dp)
                    ) { Icon(icons[icon], null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onPrimary) }
                    Spacer(Modifier.width(1.dp))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomEnd = 32.dp, topEnd = 32.dp))
                            .clip(RoundedCornerShape(bottomEnd = 32.dp, topEnd = 32.dp))
                            .padding(7.dp)
                    ) { Icon(Icons.Default.Lock, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onPrimary) }
                }
            }

            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier
                .padding(end = 8.dp)
                .widthIn(max = 150.dp), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
fun CompactAddNewWorkspaceCard(onClick: ()->Unit){
    Card(
        modifier = Modifier.clip(RoundedCornerShape(50)),
        shape = RoundedCornerShape(50),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(28.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) { Icon(Icons.Default.Add, null, Modifier.size(18.dp)) }
            Text(stringResource(R.string.add), modifier = Modifier.padding(end = 12.dp), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtremeCompactMode(
    navController: NavController,
    states: States,
    viewModels: ViewModels
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isAddNewWorkspace by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    when {
        states.workspaceState.currentWorkspace == null -> {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        title = { CompactAddNewWorkspaceCard { isAddNewWorkspace = true } },
                        navigationIcon = {},
                        actions = {
                            IconButton(
                                onClick = {navController.navigate(NavRoutes.Settings.route)},
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) { Icon(Icons.Default.Settings, null) }
                        }
                    )
                }
            ) { innerPadding ->
                Box(Modifier.padding(innerPadding)){
                    EmptySpaces()
                }
            }
        }
        else -> {
            val wrongPassKeyLabel = stringResource(R.string.Wrong_Passkey)
            val allWorkspaces = states.workspaceState.allWorkspaces
            val currentWorkspace = states.workspaceState.currentWorkspace
            val workspaceId = currentWorkspace.workspaceId
            var lockedWorkspace by remember { mutableStateOf<WorkspaceModel?>(null) }
            var showChangeWorkspaceBottomSheet by remember { mutableStateOf(false) }
            val isCompactMode = true
            val allSpaces = getSpacesList()
            val selectedNotes = states.notesState.selectedNotes
            val allNotes = states.notesState.allNotes
            var query by remember { mutableStateOf("") }
            val selectedSpaceId = rememberSaveable { mutableIntStateOf(if (currentWorkspace.selectedSpaces.isEmpty()) -1 else currentWorkspace.selectedSpaces.first()) }
            val currentSpace = allSpaces.find { it.id == selectedSpaceId.intValue }
            var isMoreExpanded by remember { mutableStateOf(false) }
            var editWorkspaceDialog by remember { mutableStateOf(false) }
            var editIconSheet by remember { mutableStateOf(false) }
            var showDeleteWorkspaceDialog by remember { mutableStateOf(false) }
            var showLockDialog by remember { mutableStateOf(false) }
            var addSpaceBottomSheet by remember { mutableStateOf(false) }
            var addProgressItem by remember { mutableStateOf(false) }
            var selectedProgressBoardItem by remember { mutableStateOf(ProgressBoardModel(workspaceId = workspaceId)) }
            var showDeleteDialog by remember { mutableStateOf(false) }
            val expandedTODOIds = rememberSaveable(workspaceId) { mutableStateOf<Set<String>>(emptySet()) }

            lockedWorkspace?.let {
                SetPasskeyDialog(onConfirmRequest = { passkey ->
                    if (it.passKey == passkey) {
                        viewModels.workspaceViewModel.onEvent(WorkspaceEvents.ChangeWorkspace(it))
                    } else {
                        Toast.makeText(context, wrongPassKeyLabel, Toast.LENGTH_SHORT).show()
                    }
                }) { lockedWorkspace = null }
            }

            LaunchedEffect(workspaceId) {
                viewModels.notesViewModel.onEvent(NotesEvents.EnterWorkspace(workspaceId))
                viewModels.journalViewModel.onEvent(JournalEvents.EnterWorkspace(workspaceId))
                viewModels.todoViewModel.onEvent(TodoEvents.EnterWorkspace(workspaceId))
                viewModels.eventViewModel.onEvent(TaskEvents.EnterWorkspace(workspaceId))
                viewModels.habitViewModel.onEvent(HabitEvents.EnterWorkspace(workspaceId))
                viewModels.progressBoardViewModel.onEvent(ProgressBoardEvents.EnterWorkspace(workspaceId))
            }

            Scaffold(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        title = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CompactWorkspaceCard(currentWorkspace.icon, currentWorkspace.passKey.isNotBlank(), currentWorkspace.title) { showChangeWorkspaceBottomSheet = true }
                                CompactAddNewWorkspaceCard { isAddNewWorkspace = true }
                            } },
                        navigationIcon = {},
                        actions = {
                            Row(Modifier.padding(end = 8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp), RoundedCornerShape(bottomStart = 32.dp, topStart = 32.dp))
                                        .clip(RoundedCornerShape(bottomStart = 32.dp, topStart = 32.dp))
                                        .clickable { isMoreExpanded = true }
                                        .padding(8.dp)
                                ) { Icon(Icons.Default.MoreVert, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary) }
                                ExtremeCompactModeDropDown (
                                    isMoreExpanded,
                                    isLocked = currentWorkspace.passKey.isNotBlank(),
                                    showEditLabel = currentWorkspace.selectedSpaces.contains(1),
                                    onDelete = { showDeleteWorkspaceDialog = true },
                                    onEditDetails = { editWorkspaceDialog = true },
                                    onEditLabel = { navController.navigate(NavRoutes.EditLabels.withArgs(workspaceId)) },
                                    onToggleLock = {
                                        if (currentWorkspace.passKey.isNotBlank()) {
                                            viewModels.workspaceViewModel.onEvent(WorkspaceEvents.UpsertSpace(currentWorkspace.copy(passKey = "")))
                                        } else showLockDialog = true
                                    }
                                ){ isMoreExpanded = false}
                                Spacer(Modifier.width(1.dp))
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp), RoundedCornerShape(bottomEnd = 32.dp, topEnd = 32.dp))
                                        .clip(RoundedCornerShape(bottomEnd = 32.dp, topEnd = 32.dp))
                                        .clickable { navController.navigate(NavRoutes.Settings.route) }
                                        .padding(8.dp)
                                ) { Icon(Icons.Default.Settings, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary) }
                            }
                        }
                    )
                }
            ) { innerPadding ->
                LazyColumn(Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 8.dp)) {
                    spacesToolbarList(
                        navController,
                        isCompactMode,
                        selectedSpaceId.intValue,
                        workspaceId,
                        currentWorkspace,
                        states,
                        viewModels.settingsViewModel::onEvent,
                        viewModels.notesViewModel::onEvent,
                        { selectedSpaceId.intValue = it },
                        { addSpaceBottomSheet = true },
                        {
                            addProgressItem = true
                            selectedProgressBoardItem =
                                ProgressBoardModel(workspaceId = workspaceId)
                        }
                    )
                    spaceDetailItems(
                        navController,
                        workspaceId,
                        currentWorkspace.selectedSpaces,
                        states,
                        query,
                        states.settings.data.cornerRadius,
                        currentSpace,
                        viewModels.habitViewModel::onEvent,
                        viewModels.notesViewModel::onEvent,
                        viewModels.journalViewModel::onEvent,
                        viewModels.eventViewModel::onEvent,
                        viewModels.todoViewModel::onEvent,
                        expandedTODOIds.value,
                        { id ->
                            expandedTODOIds.value =
                                if (id in expandedTODOIds.value) expandedTODOIds.value - id else expandedTODOIds.value + id
                        },
                        {
                            addProgressItem = true
                            selectedProgressBoardItem = it
                        }
                    )
                }
            }

            if (showLockDialog) {
                SetPasskeyDialog(
                    {
                        viewModels.workspaceViewModel.onEvent(
                            WorkspaceEvents.UpsertSpace(
                                currentWorkspace.copy(
                                    passKey = it
                                )
                            )
                        )
                    })
                { showLockDialog = false }
            }

            if (showDeleteWorkspaceDialog) {
                DeleteAlert(onConfirmation = {
                    showDeleteWorkspaceDialog = false
                    viewModels.workspaceViewModel.onEvent(WorkspaceEvents.DeleteSpace(currentWorkspace))
                    viewModels.notesViewModel.onEvent(NotesEvents.DeleteAllWorkspaceNotes(workspaceId))
                    viewModels.todoViewModel.onEvent(TodoEvents.DeleteAllWorkspaceLists(workspaceId))
                    viewModels.eventViewModel.onEvent(TaskEvents.DeleteAllWorkspaceEvents(workspaceId, context))
                    viewModels.habitViewModel.onEvent(HabitEvents.DeleteAllWorkspaceHabits(workspaceId, context))
                    viewModels.journalViewModel.onEvent(JournalEvents.DeleteWorkspaceEntries(workspaceId))
                    viewModels.progressBoardViewModel.onEvent(ProgressBoardEvents.DeleteBoardItemsByWorkspace(workspaceId))
                }, onDismissRequest = {
                    showDeleteWorkspaceDialog = false
                })
            }

            AddNewSpacesBottomSheet(
                isVisible = addSpaceBottomSheet,
                sheetState = sheetState,
                selectedSpaces = allSpaces.filter { currentWorkspace.selectedSpaces.contains(it.id) },
                onDismiss = { addSpaceBottomSheet = false },
                onRemove = { spaceId ->
                    val newSelected = currentWorkspace.selectedSpaces.firstOrNull { it != spaceId } ?: -1
                    selectedSpaceId.intValue = newSelected

                    viewModels.workspaceViewModel.onEvent(
                        WorkspaceEvents.UpsertSpace(
                            currentWorkspace.copy(
                                selectedSpaces = currentWorkspace.selectedSpaces.minus(
                                    spaceId
                                )
                            )
                        )
                    )

                    removeSpaceData(
                        workspaceId,
                        spaceId,
                        context,
                        viewModels.eventViewModel::onEvent,
                        viewModels.todoViewModel::onEvent,
                        viewModels.habitViewModel::onEvent,
                        viewModels.notesViewModel::onEvent,
                        viewModels.journalViewModel::onEvent,
                        viewModels.progressBoardViewModel::onEvent
                    )
                },
                onSelect = {
                    if (selectedSpaceId.intValue == -1) selectedSpaceId.intValue = it
                    viewModels.workspaceViewModel.onEvent(
                        WorkspaceEvents.UpsertSpace(
                            currentWorkspace.copy(
                                selectedSpaces = currentWorkspace.selectedSpaces.plus(
                                    it
                                )
                            )
                        )
                    )
                }
            )

            if(showChangeWorkspaceBottomSheet){
                ChangeWorkspaceDialog(
                    currentWorkspace,
                    allWorkspaces,
                    { showChangeWorkspaceBottomSheet=false },
                    {
                        if(it.passKey.isNotBlank()){
                            lockedWorkspace = it
                            return@ChangeWorkspaceDialog
                        }
                        else { viewModels.workspaceViewModel.onEvent(WorkspaceEvents.ChangeWorkspace(it)) }
                    },
                )
            }

            // Edit Workspace BottomSheet
            key(workspaceId) {
                NewWorkspaceBottomSheet(
                    isEditing = true,
                    workspace = currentWorkspace,
                    isVisible = editWorkspaceDialog,
                    sheetState = sheetState,
                    onDismiss = {
                        scope.launch { sheetState.hide() }
                            .invokeOnCompletion { editWorkspaceDialog = false }
                    },
                    onConfirm = {
                        viewModels.workspaceViewModel.onEvent(WorkspaceEvents.UpsertSpace(it))
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            editWorkspaceDialog = false
                        }
                    }
                )
            }

            // Edit Workspace Sheet
            ChangeIconBottomSheet(
                isVisible = editIconSheet,
                sheetState = sheetState,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { editIconSheet = false }
                },
                onConfirm = { index ->
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        viewModels.workspaceViewModel.onEvent(
                            WorkspaceEvents.UpsertSpace(
                                currentWorkspace.copy(
                                    icon = index
                                )
                            )
                        )
                        editIconSheet = false
                    }
                }
            )

            NewBoardItemSheet(
                addProgressItem, sheetState, selectedProgressBoardItem,
                { addProgressItem = false }, {
                    addProgressItem = false
                    viewModels.progressBoardViewModel.onEvent(
                        ProgressBoardEvents.UpsertProgressItem(
                            it
                        )
                    )
                    selectedProgressBoardItem = ProgressBoardModel(workspaceId = workspaceId)
                }) {
                viewModels.progressBoardViewModel.onEvent(ProgressBoardEvents.DeleteProgressItem(it))
            }

            if (showDeleteDialog) {
                DeleteAlert(onConfirmation = {
                    viewModels.notesViewModel.onEvent(NotesEvents.DeleteNotes(allNotes.filter {
                        selectedNotes.contains(
                            it.notesId
                        )
                    }))
                    viewModels.notesViewModel.onEvent(NotesEvents.ClearSelection)
                    showDeleteDialog = false
                }, onDismissRequest = {
                    showDeleteDialog = false
                })
            }
        }
    }

    NewWorkspaceBottomSheet(isVisible = isAddNewWorkspace, sheetState = sheetState, onDismiss = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                isAddNewWorkspace = false
            }
        }
    }, onConfirm = { viewModels.workspaceViewModel.onEvent(WorkspaceEvents.UpsertSpace(it)) })
}
