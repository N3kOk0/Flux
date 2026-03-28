package com.flux.ui.screens.workspaces

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.flux.ui.state.States
import com.flux.ui.viewModel.ViewModels

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkSpaces(
    snackbarHostState: SnackbarHostState,
    navController: NavController,
    states: States,
    viewModels: ViewModels,
) {
    val radius = states.settings.data.cornerRadius
    val gridColumns = states.settings.data.workspaceGridColumns
    val isExtremeCompactMode = gridColumns==3
    when {
        isExtremeCompactMode ->
            ExtremeCompactMode(
                navController,
                states,
                viewModels
            )
        else ->
            NormalMode(
                snackbarHostState,
                navController,
                gridColumns,
                radius,
                states.workspaceState.allWorkspaces,
                viewModels.workspaceViewModel::onEvent
            )
    }
}