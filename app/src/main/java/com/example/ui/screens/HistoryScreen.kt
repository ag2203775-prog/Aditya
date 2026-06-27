package com.example.ui.screens

import android.provider.CalendarContract.Instances
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalculationHistory
import com.example.viewmodel.CalculatorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: CalculatorViewModel,
    onNavigateToTab: (Int) -> Unit, // Callback to switch tab dynamically
    modifier: Modifier = Modifier
) {
    val historyList by viewModel.historyState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HEADER BAR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Calculations History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Offline local ledger",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (historyList.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearAllHistory() },
                    modifier = Modifier.testTag("btn_clear_all_history")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = "Clear All",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Clear All")
                }
            }
        }

        // --- RECORDS CONTAINER ---
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HistoryToggleOff,
                            contentDescription = "Empty History",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = "Your calculation history is empty",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Run solve operations or camera equations in the previous sections. They'll show up here automatically for offline study sessions!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("history_list_container"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyList, key = { it.id }) { record ->
                    HistoryItemCard(
                        record = record,
                        onReload = {
                            viewModel.loadHistoryItem(record)
                            onNavigateToTab(1)
                        },
                        onDelete = { viewModel.deleteHistoryItem(record.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    record: CalculationHistory,
    onReload: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(record.timestamp) {
        val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        formatter.format(Date(record.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReload() }
            .testTag("history_card_${record.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (record.isOcrOrStep) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (record.isOcrOrStep) {
                        Icons.Outlined.AutoAwesome
                    } else {
                        Icons.Outlined.Calculate
                    },
                    contentDescription = "Icon",
                    tint = if (record.isOcrOrStep) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (record.isOcrOrStep) "AI Solver Step solution" else "Scientific Calc",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = record.expression,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (record.isOcrOrStep) "Steps recorded offline" else "= ${record.result}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("delete_history_item_${record.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete record",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
