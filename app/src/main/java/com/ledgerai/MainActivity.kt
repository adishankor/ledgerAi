@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ledgerai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.ledgerai.data.local.TransactionEntity
import com.ledgerai.presentation.theme.LedgerAITheme
import com.ledgerai.service.DraftTransaction
import com.ledgerai.service.LocalDraftManager
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LedgerAITheme { MainScreen() } }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    var showAddSheet by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        LocalDraftManager.drafts.collectLatest { draft ->
            viewModel.addDraft(draft)
        }
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Add, contentDescription = "Add") }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.surface) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(navController, startDestination = "dashboard") {
                composable("dashboard") { DashboardScreen() }
                composable("analytics") { AnalyticsScreen() }
                composable("transactions") { TransactionListScreen(viewModel) }
            }
            SettingsGear(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp))
        }
    }
    if (showAddSheet) AddTransactionBottomSheet(onDismiss = { showAddSheet = false }, viewModel)
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val items = listOf("Dashboard", "Analytics", "Transactions")
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                label = { Text(item) },
                selected = navController.currentBackStackEntryAsState().value?.destination?.route == item.lowercase(),
                onClick = { navController.navigate(item.lowercase()) }
            )
        }
    }
}

@Composable
fun SettingsGear(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.size(48.dp), shape = CircleShape, shadowElevation = 8.dp) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
}

@Composable
fun DashboardScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Dashboard - Coming Soon")
    }
}

@Composable
fun AnalyticsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Analytics - Coming Soon")
    }
}

@Composable
fun TransactionListScreen(viewModel: MainViewModel) {
    val transactions = viewModel.transactions.collectAsState()
    LazyColumn {
        items(transactions.value) { txn ->
            TransactionItem(txn)
        }
    }
}

@Composable
fun TransactionItem(txn: TransactionEntity) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.padding(16.dp)) {
            val isExpense = txn.debitAccountId.startsWith("Expense")
            Text(
                text = if (isExpense) "- $${txn.amount}" else "+ $${txn.amount}",
                color = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(txn.description)
                Text("${txn.creditAccountId} → ${txn.debitAccountId}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun AddTransactionBottomSheet(onDismiss: () -> Unit, viewModel: MainViewModel) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Add Transaction", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                AddOptionItem("Manual", Icons.Default.Edit) { /* Manual form */ }
                AddOptionItem("Scan", Icons.Default.CameraAlt) { /* OCR */ }
                AddOptionItem("Auto-Detect", Icons.Default.Notifications) { /* Show drafts */ }
            }
            val drafts = viewModel.draftTransactions.collectAsState()
            drafts.value.take(3).forEach { draft ->
                DraftTransactionCard(draft, onApprove = { viewModel.approveDraft(draft) })
            }
        }
    }
}

@Composable
fun AddOptionItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) { Icon(icon, contentDescription = label) }
        Text(label)
    }
}

@Composable
fun DraftTransactionCard(draft: DraftTransaction, onApprove: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (draft.isCredit) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(draft.rawText.take(40) + "...", maxLines = 1)
                Text("${draft.currency} ${draft.amount}", fontWeight = FontWeight.Bold)
            }
            Button(onClick = onApprove, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                Text("Approve")
            }
        }
    }
}
