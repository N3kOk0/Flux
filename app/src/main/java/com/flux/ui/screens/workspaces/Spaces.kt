package com.flux.ui.screens.workspaces

import androidx.compose.foundation.lazy.LazyListScope
import androidx.navigation.NavController
import com.flux.data.model.ProgressBoardModel
import com.flux.data.model.Space
import com.flux.ui.events.HabitEvents
import com.flux.ui.events.JournalEvents
import com.flux.ui.events.NotesEvents
import com.flux.ui.events.TaskEvents
import com.flux.ui.events.TodoEvents
import com.flux.ui.screens.analytics.analyticsItems
import com.flux.ui.screens.events.eventHomeItems
import com.flux.ui.screens.habits.habitsHomeItems
import com.flux.ui.screens.journal.journalHomeItems
import com.flux.ui.screens.notes.notesHomeItems
import com.flux.ui.screens.progressBoard.progressBoardItems
import com.flux.ui.screens.todo.todoHomeItems
import com.flux.ui.state.States

fun LazyListScope.spaceDetailItems(
    navController: NavController,
    workspaceId: String,
    availableSpaces: List<Int>,
    states: States,
    query: String,
    radius: Int,
    space: Space?,
    onHabitEvents: (HabitEvents) -> Unit,
    onNotesEvents: (NotesEvents) -> Unit,
    onJournalEvents: (JournalEvents) -> Unit,
    onTaskEvents: (TaskEvents) -> Unit,
    onTodoEvents: (TodoEvents) -> Unit,
    expandedTODOIds: Set<String>,
    onToggleTODO: (String) -> Unit,
    onProgressItemClick: (ProgressBoardModel) -> Unit,
) {
    when (space?.id) {
        5 -> habitsHomeItems(
            navController,
            states.habitState.isLoading,
            radius,
            workspaceId,
            states.habitState.allHabits,
            states.habitState.allInstances,
            states.settings,
            onHabitEvents
        )
        1 -> notesHomeItems(
            navController,
            workspaceId,
            states.notesState.selectedNotes,
            query,
            radius,
            states.settings.data.isGridView,
            states.notesState.allLabels,
            states.notesState.isNotesLoading,
            states.notesState.allNotes,
            onNotesEvents
        )
        4 -> journalHomeItems(
            navController,
            states.settings,
            states.journalState.selectedYearMonth,
            states.journalState.selectedDate,
            states.journalState.isLoading,
            workspaceId,
            states.journalState.monthlyJournalCount,
            states.journalState.datedEntries,
            onJournalEvents
        )
        6 -> analyticsItems(
            availableSpaces,
            radius,
            states.habitState.allInstances,
            states.habitState.allHabits.size,
            states.notesState.allNotes.size,
            states.journalState.allEntries,
            states.habitState.allHabits,
            states.eventState.allEvent,
            states.eventState.allEventInstances
        )
        7 -> progressBoardItems(
            states.progressBoardState.isLoading,
            radius,
            states.progressBoardState.allItems,
            onProgressItemClick
        )
        2 -> todoHomeItems(
            navController, radius, states.todoState.allLists, workspaceId,
            states.todoState.isLoading, expandedTODOIds, onTodoEvents, onToggleTODO
        )
        3 -> eventHomeItems(
            navController, radius,
            states.settings.data.isCalendarMonthlyView,
            states.settings.data.is24HourFormat,
            states.eventState.isDatedEventLoading,
            workspaceId,
            states.eventState.selectedYearMonth,
            states.eventState.selectedDate,
            states.eventState.monthlyEventDates,
            states.eventState.datedEvents,
            states.eventState.allEventInstances,
            onTaskEvents
        )
    }
}