package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryDatabase
import com.example.data.HistoryRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CalculatorViewModel
import com.example.viewmodel.CalculatorViewModelFactory

class MainActivity : ComponentActivity() {
    
    // Lazy initialize standard Room modules
    private val database by lazy { HistoryDatabase.getDatabase(this) }
    private val repository by lazy { HistoryRepository(database.historyDao()) }
    
    // Viewmodel instantiation factory
    private val viewModel: CalculatorViewModel by viewModels {
        CalculatorViewModelFactory(repository)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var selectedTab by rememberSaveable { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = "Advanced Calculator",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("main_navigation_bar"),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp
                        ) {
                            val items = listOf(
                                NavigationTabItem("Normal", Icons.Filled.Calculate, Icons.Outlined.Calculate, 0),
                                NavigationTabItem("Scientific", Icons.Filled.Calculate, Icons.Outlined.Calculate, 1),
                                NavigationTabItem("Advanced", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, 2),
                                NavigationTabItem("History", Icons.Filled.History, Icons.Outlined.History, 3)
                            )

                            items.forEach { tabItem ->
                                val isSelected = selectedTab == tabItem.index
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { selectedTab = tabItem.index },
                                    label = { Text(tabItem.title, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium) },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tabItem.selectedIcon else tabItem.unselectedIcon,
                                            contentDescription = tabItem.title
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_tab_${tabItem.title.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    val modifierWithInsets = Modifier.padding(innerPadding)
                    
                    when (selectedTab) {
                        0 -> NormalCalcScreen(viewModel, modifierWithInsets)
                        1 -> ScienceCalcScreen(viewModel, modifierWithInsets)
                        2 -> AdvancedCalcScreen(viewModel, modifierWithInsets)
                        3 -> HistoryScreen(
                            viewModel = viewModel,
                            onNavigateToTab = { index -> selectedTab = index },
                            modifier = modifierWithInsets
                        )
                    }
                }
            }
        }
    }
}

data class NavigationTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val index: Int
)
