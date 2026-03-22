package com.flux.ui.screens.progressBoard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.flux.R
import com.flux.data.model.ProgressBoardModel
import com.flux.navigation.Loader
import com.flux.other.icons
import com.flux.ui.components.BoardStatusItem
import com.flux.ui.components.convertMillisToDate
import com.flux.ui.components.shapeManager
import com.flux.ui.theme.completed
import com.flux.ui.theme.failed
import com.flux.ui.theme.pending

fun LazyListScope.progressBoardItems(
    isLoading: Boolean,
    radius: Int,
    boardItems: List<ProgressBoardModel>,
    onClick: (ProgressBoardModel)->Unit
){
    when {
        isLoading -> item { Loader() }
        else ->
            item {
                val notStartedItems = boardItems.filter { it.status == 0 }
                val inProgressItems = boardItems.filter { it.status == 1 }
                val completedItems = boardItems.filter { it.status == 2 }

                LazyRow(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (notStartedItems.isNotEmpty()) {
                        item {
                            BoardContainer(
                                failed,
                                stringResource(R.string.not_started),
                                radius,
                                notStartedItems,
                                onClick
                            )
                        }
                    }
                    if (inProgressItems.isNotEmpty()) {
                        item {
                            BoardContainer(
                                pending,
                                stringResource(R.string.in_progress),
                                radius,
                                inProgressItems,
                                onClick
                            )
                        }
                    }

                    if (completedItems.isNotEmpty()) {
                        item {
                            BoardContainer(
                                completed,
                                stringResource(R.string.Completed),
                                radius,
                                completedItems,
                                onClick
                            )
                        }
                    }
                }
            }
    }
}

@Composable
fun BoardContainer(
    containerColor: Color,
    status: String,
    radius: Int,
    items: List<ProgressBoardModel>,
    onClick: (ProgressBoardModel) -> Unit
){
    Card(
        modifier = Modifier.width(300.dp),
        shape = shapeManager(radius = radius * 2),
        onClick = { },
        colors = CardDefaults.cardColors(containerColor = containerColor.copy(0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BoardStatusItem(true, status, containerColor){}
            Spacer(Modifier.height(4.dp))
            items.forEach { item->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shape = shapeManager(radius = radius * 2),
                    onClick = { onClick(item) },
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icons[item.icon],
                            null,
                            modifier = Modifier.alpha(0.8f)
                        )
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(item.title, style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (item.startDate != -1L) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(Icons.Default.Timelapse, null, modifier = Modifier.size(16.dp).alpha(0.75f))
                                        Text(
                                            convertMillisToDate(item.startDate),
                                            modifier = Modifier.alpha(0.75f),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                                if (item.endDate != -1L) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(Icons.Default.Flag, null, modifier = Modifier.size(16.dp).alpha(0.75f))
                                        Text(
                                            convertMillisToDate(item.endDate),
                                            modifier = Modifier.alpha(0.75f),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
