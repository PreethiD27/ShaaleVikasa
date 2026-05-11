package com.shaalevikas.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaalevikas.app.data.Need
import com.shaalevikas.app.data.CATEGORIES
import com.shaalevikas.app.ui.MainViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: MainViewModel, onNeedClick: (String) -> Unit,
                    onAdminClick: () -> Unit, onImpactClick: () -> Unit, onLogout: () -> Unit) {
    val needs by vm.filteredNeeds.collectAsState()
    val search by vm.searchQuery.collectAsState()
    val filterCat by vm.filterCategory.collectAsState()
    val filterStatus by vm.filterStatus.collectAsState()
    val opMsg by vm.opMessage.collectAsState()
    if (opMsg != null) LaunchedEffect(opMsg) { kotlinx.coroutines.delay(2500); vm.clearOpMessage() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("\uD83C\uDFEB Shaale-Vikas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary),
                actions = {
                    if (vm.isAdmin) IconButton(onClick = onAdminClick) {
                        Icon(Icons.Default.AdminPanelSettings, "Admin", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onImpactClick) {
                        Icon(Icons.Default.PhotoLibrary, "Impact", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Logout", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                })
        },
        snackbarHost = { if (opMsg != null) Snackbar(Modifier.padding(8.dp)) { Text(opMsg!!) } }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(value = search, onValueChange = { vm.setSearch(it) },
                placeholder = { Text("Search needs...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth().padding(12.dp, 8.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip("All", filterCat == "All") { vm.setFilterCategory("All") } }
                items(CATEGORIES) { cat -> FilterChip(cat, filterCat == cat) { vm.setFilterCategory(cat) } }
            }
            Row(Modifier.padding(12.dp, 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Active", "Fulfilled").forEach { s ->
                    FilterChip(s, filterStatus == s) { vm.setFilterStatus(s) }
                }
            }
            if (needs.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No needs found.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            } else LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(needs) { need -> NeedCard(need = need, onClick = { onNeedClick(need.id) }) }
            }
        }
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label, fontSize = 12.sp) })
}

@Composable
fun NeedCard(need: Need, onClick: () -> Unit) {
    val fmt = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(need.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                StatusBadge(need.status)
            }
            Text(need.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(6.dp))
            Text(need.description, fontSize = 13.sp, maxLines = 2)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = { need.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (need.progressPercent >= 100) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${need.progressPercent}% funded", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary)
                Text("${fmt.format(need.pledgedAmount)} / ${fmt.format(need.targetAmount)}", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = if (status == "Fulfilled") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Text(status, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
    }
}
