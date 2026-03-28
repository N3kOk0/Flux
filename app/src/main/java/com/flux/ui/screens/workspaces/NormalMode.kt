package com.flux.ui.screens.workspaces

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flux.data.model.WorkspaceModel
import com.flux.navigation.NavRoutes
import com.flux.ui.components.EmptySpaces
import com.flux.ui.components.NewWorkspaceBottomSheet
import com.flux.ui.components.SelectedBar
import com.flux.ui.components.SetPasskeyDialog
import com.flux.ui.components.WorkspaceCard
import com.flux.ui.events.WorkspaceEvents
import kotlinx.coroutines.launch
import com.flux.R
import com.flux.ui.components.GeneralSearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalMode(
    snackbarHostState: SnackbarHostState,
    navController: NavController,
    gridColumns: Int,
    radius: Int,
    allSpaces: List<WorkspaceModel>,
    onWorkSpaceEvents: (WorkspaceEvents) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var isAddNewWorkspace by remember { mutableStateOf(false) }
    val selectedWorkspace = remember { mutableStateListOf<WorkspaceModel>() }
    var lockedWorkspace by remember { mutableStateOf<WorkspaceModel?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val wrongPassKeyLabel = stringResource(R.string.Wrong_Passkey)

    lockedWorkspace?.let {
        SetPasskeyDialog(onConfirmRequest = { passkey ->
            if (it.passKey == passkey) {
                navController.navigate(NavRoutes.WorkspaceHome.withArgs(it.workspaceId))
            } else {
                Toast.makeText(context, wrongPassKeyLabel, Toast.LENGTH_SHORT).show()
            }
        }) { lockedWorkspace = null }
    }

    fun handleWorkspaceClick(space: WorkspaceModel) {
        if (space.passKey.isNotBlank()) { lockedWorkspace = space
        } else { navController.navigate(NavRoutes.WorkspaceHome.withArgs(space.workspaceId)) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            if (selectedWorkspace.isEmpty()) {
                GeneralSearchBar(
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = Icons.Default.Settings,
                    textFieldState = TextFieldState(query),
                    onSearch = { query = it },
                    onLeadingIconClicked = {  },
                    onTrailingIconClicked = { navController.navigate(NavRoutes.Settings.route) },
                    onCloseClicked = { query = "" }
                )
            } else{
                Box(Modifier.padding(top = 42.dp)){
                    SelectedBar(
                        false,
                        selectedWorkspace.containsAll(allSpaces),
                        selectedWorkspace.all { it.isPinned },
                        selectedWorkspace.size,
                        onPinClick = { onWorkSpaceEvents(WorkspaceEvents.UpsertSpaces(selectedWorkspace.toList())) },
                        onDeleteClick = {},
                        onSelectAllClick = {
                            if (selectedWorkspace.containsAll(allSpaces)){ selectedWorkspace.clear() }
                            else {
                                selectedWorkspace.clear()
                                selectedWorkspace.addAll(allSpaces)
                            }
                        },
                        onCloseClick = { selectedWorkspace.clear() }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton({ isAddNewWorkspace = true }){
                Icon(Icons.Default.Add, null)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (allSpaces.isEmpty()) { EmptySpaces() } else {
            val vSpacing = when (gridColumns) {
                1 -> 12.dp
                2 -> 16.dp
                else -> 10.dp
            }
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(gridColumns),
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalItemSpacing = vSpacing
            ) {
                if (allSpaces.none {
                        it.title.contains(query, ignoreCase = true) ||
                                it.description.contains(query, ignoreCase = true)
                    }) {
                    item(span = StaggeredGridItemSpan.FullLine) { EmptySpaces() }
                }

                if (allSpaces.any {
                        it.isPinned && (it.title.contains(query, ignoreCase = true) ||
                                it.description.contains(query, ignoreCase = true))
                    }) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Text(
                            stringResource(R.string.Pinned),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                items(allSpaces.filter {
                    it.isPinned && (it.title.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true))
                }) { space ->
                    WorkspaceCard(
                        gridColumns = gridColumns,
                        iconIndex = space.icon,
                        radius = radius,
                        isLocked = space.passKey.isNotBlank(),
                        cover = space.cover,
                        title = space.title,
                        description = space.description,
                        isSelected = selectedWorkspace.contains(space),
                        onClick = { handleWorkspaceClick(space) },
                        onLongPressed = {
                            if (selectedWorkspace.contains(space)) selectedWorkspace.remove(space)
                            else selectedWorkspace.add(space)
                        }
                    )
                }

                if (allSpaces.any {
                        it.isPinned && (it.title.contains(query, ignoreCase = true) ||
                                it.description.contains(query, ignoreCase = true))
                    }) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Text(
                            stringResource(R.string.Others),
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                items(allSpaces.filter {
                    !it.isPinned && (it.title.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true))
                }) { space ->
                    WorkspaceCard(
                        gridColumns = gridColumns,
                        iconIndex = space.icon,
                        radius = radius,
                        isLocked = space.passKey.isNotBlank(),
                        cover = space.cover,
                        title = space.title,
                        description = space.description,
                        isSelected = selectedWorkspace.contains(space),
                        onClick = { handleWorkspaceClick(space) },
                        onLongPressed = {
                            if (selectedWorkspace.contains(space)) selectedWorkspace.remove(space)
                            else selectedWorkspace.add(space)
                        }
                    )
                }
            }
        }
    }

    NewWorkspaceBottomSheet(isVisible = isAddNewWorkspace, sheetState = sheetState, onDismiss = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                isAddNewWorkspace = false
            }
        }
    }, onConfirm = { onWorkSpaceEvents(WorkspaceEvents.UpsertSpace(it)) })
}