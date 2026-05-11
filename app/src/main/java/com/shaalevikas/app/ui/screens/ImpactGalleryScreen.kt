package com.shaalevikas.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shaalevikas.app.data.Need
import com.shaalevikas.app.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImpactGalleryScreen(vm: MainViewModel, onBack: () -> Unit) {
    val needs by vm.needs.collectAsState()
    val fulfilled = needs.filter { it.status == "Fulfilled" }
    val active = needs.filter { it.status == "Active" && vm.isAdmin }
    val opMsg by vm.opMessage.collectAsState()
    if (opMsg != null) LaunchedEffect(opMsg) { kotlinx.coroutines.delay(2500); vm.clearOpMessage() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Impact Gallery", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary))
        },
        snackbarHost = { if (opMsg != null) Snackbar(Modifier.padding(8.dp)) { Text(opMsg!!) } }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (vm.isAdmin && active.isNotEmpty()) {
                item { Text("Mark as Fulfilled", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary) }
                items(active) { need -> MarkFulfilledCard(need, vm) }
                item { Divider() }
            }
            item { Text("Completed Projects", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            if (fulfilled.isEmpty()) item {
                Text("No completed projects yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            } else items(fulfilled) { need -> ImpactCard(need, vm) }
        }
    }
}

@Composable
fun ImpactCard(need: Need, vm: MainViewModel) {
    var aiSummary by remember { mutableStateOf("") }
    val aiLoading by vm.aiLoading.collectAsState()
    Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text(need.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            if (need.beforePhotoUrl.isNotBlank() || need.afterPhotoUrl.isNotBlank()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (need.beforePhotoUrl.isNotBlank()) Column(Modifier.weight(1f)) {
                        Text("Before", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        AsyncImage(model = need.beforePhotoUrl, contentDescription = "Before",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(8.dp)))
                    }
                    if (need.afterPhotoUrl.isNotBlank()) Column(Modifier.weight(1f)) {
                        Text("After", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                        AsyncImage(model = need.afterPhotoUrl, contentDescription = "After",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(8.dp)))
                    }
                }
            }
            if (aiSummary.isNotBlank()) Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                Text(aiSummary, Modifier.padding(10.dp), fontSize = 13.sp, lineHeight = 20.sp)
            }
            if (aiSummary.isBlank()) OutlinedButton(
                onClick = { vm.generateImpactSummary(need.title, need.description) { aiSummary = it } },
                enabled = !aiLoading, modifier = Modifier.fillMaxWidth()) {
                Text(if (aiLoading) "Generating\u2026" else "\u2728 Generate Impact Summary")
            }
        }
    }
}

@Composable
fun MarkFulfilledCard(need: Need, vm: MainViewModel) {
    var afterUri by remember { mutableStateOf<Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { afterUri = it }
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(need.title, fontWeight = FontWeight.SemiBold)
            OutlinedButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (afterUri != null) "After Photo Selected \u2713" else "Select After Photo")
            }
            Button(onClick = { vm.markFulfilled(need, afterUri) }, modifier = Modifier.fillMaxWidth()) {
                Text("Mark as Fulfilled")
            }
        }
    }
}
