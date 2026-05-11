package com.shaalevikas.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaalevikas.app.data.CATEGORIES
import com.shaalevikas.app.data.Need
import com.shaalevikas.app.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(vm: MainViewModel, onAddClick: () -> Unit, onEditClick: (String) -> Unit, onBack: () -> Unit) {
    val needs by vm.needs.collectAsState()
    val opMsg by vm.opMessage.collectAsState()
    if (opMsg != null) LaunchedEffect(opMsg) { kotlinx.coroutines.delay(2500); vm.clearOpMessage() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Admin Panel", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary))
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick, containerColor = MaterialTheme.colorScheme.secondary) {
                Icon(Icons.Default.Add, "Add Need")
            }
        },
        snackbarHost = { if (opMsg != null) Snackbar(Modifier.padding(8.dp)) { Text(opMsg!!) } }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (needs.isEmpty()) item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No needs yet. Tap + to add one.")
                }
            }
            items(needs) { need -> AdminNeedCard(need = need, onEdit = { onEditClick(need.id) }, onDelete = { vm.deleteNeed(need.id) }) }
        }
    }
}

@Composable
fun AdminNeedCard(need: Need, onEdit: () -> Unit, onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(need.title, fontWeight = FontWeight.Bold)
                Text("${need.category} \u2022 ${need.status}", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text("${need.progressPercent}% funded", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false },
        title = { Text("Delete Need?") },
        text = { Text("This will permanently delete \"${need.title}\".") },
        confirmButton = {
            Button(onClick = { onDelete(); confirmDelete = false },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNeedScreen(vm: MainViewModel, editNeedId: String?, onDone: () -> Unit) {
    val needs by vm.needs.collectAsState()
    val existingNeed = editNeedId?.let { id -> needs.find { it.id == id } }
    var title by remember { mutableStateOf(existingNeed?.title ?: "") }
    var description by remember { mutableStateOf(existingNeed?.description ?: "") }
    var category by remember { mutableStateOf(existingNeed?.category ?: CATEGORIES[0]) }
    var targetAmount by remember { mutableStateOf(existingNeed?.targetAmount?.toString() ?: "") }
    var beforePhotoUrl by remember { mutableStateOf(existingNeed?.beforePhotoUrl ?: "") }
    var aiPrompt by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    val aiLoading by vm.aiLoading.collectAsState()
    val aiResult by vm.aiResult.collectAsState()
    val opMsg by vm.opMessage.collectAsState()
    LaunchedEffect(aiResult) { if (aiResult != null) { description = aiResult!!; vm.clearAiResult() } }
    if (opMsg != null) LaunchedEffect(opMsg) { kotlinx.coroutines.delay(2000); vm.clearOpMessage(); onDone() }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val needId = editNeedId ?: "new_${System.currentTimeMillis()}"
            vm.uploadBeforePhoto(needId, uri) { url -> beforePhotoUrl = url }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (editNeedId != null) "Edit Need" else "Add New Need") },
                navigationIcon = { IconButton(onClick = onDone) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary))
        },
        snackbarHost = { if (opMsg != null) Snackbar(Modifier.padding(8.dp)) { Text(opMsg!!) } }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("Need Title *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                OutlinedTextField(value = category, onValueChange = {}, readOnly = true,
                    label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor())
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    CATEGORIES.forEach { cat -> DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; categoryExpanded = false }) }
                }
            }
            OutlinedTextField(targetAmount, { targetAmount = it }, label = { Text("Target Amount (\u20B9) *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(10.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("\u2728 AI Description Generator", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(aiPrompt, { aiPrompt = it }, placeholder = { Text("e.g. broken windows in classroom 3") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { vm.generateDescription(aiPrompt) }, enabled = aiPrompt.isNotBlank() && !aiLoading, modifier = Modifier.fillMaxWidth()) {
                        if (aiLoading) { CircularProgressIndicator(Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary); Spacer(Modifier.width(8.dp)) }
                        Text(if (aiLoading) "Generating\u2026" else "Generate with AI")
                    }
                }
            }
            OutlinedTextField(description, { description = it }, label = { Text("Description *") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            OutlinedButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PhotoCamera, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                Text(if (beforePhotoUrl.isNotBlank()) "Before Photo Uploaded \u2713" else "Upload Before Photo")
            }
            Button(onClick = {
                val amount = targetAmount.toDoubleOrNull() ?: 0.0
                if (editNeedId != null && existingNeed != null)
                    vm.updateNeed(existingNeed.copy(title = title, description = description, category = category, targetAmount = amount, beforePhotoUrl = beforePhotoUrl))
                else
                    vm.addNeed(Need(title = title, description = description, category = category, targetAmount = amount, beforePhotoUrl = beforePhotoUrl))
            }, enabled = title.isNotBlank() && description.isNotBlank() && targetAmount.toDoubleOrNull() != null,
                modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(if (editNeedId != null) "Update Need" else "Publish Need", fontSize = 16.sp)
            }
        }
    }
}
