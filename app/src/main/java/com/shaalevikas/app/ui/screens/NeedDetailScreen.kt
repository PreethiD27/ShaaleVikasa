package com.shaalevikas.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.shaalevikas.app.data.Pledge
import com.shaalevikas.app.ui.MainViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeedDetailScreen(vm: MainViewModel, needId: String, onBack: () -> Unit) {
    val needs by vm.needs.collectAsState()
    val need = needs.find { it.id == needId } ?: return
    LaunchedEffect(needId) { vm.loadPledgesFor(needId) }
    val pledges by vm.pledges.collectAsState()
    val opMsg by vm.opMessage.collectAsState()
    var showPledgeDialog by remember { mutableStateOf(false) }
    val fmt = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    if (opMsg != null) LaunchedEffect(opMsg) { kotlinx.coroutines.delay(2500); vm.clearOpMessage() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(need.title, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary))
        },
        floatingActionButton = {
            if (need.status == "Active") ExtendedFloatingActionButton(
                onClick = { showPledgeDialog = true },
                icon = { Icon(Icons.Default.Favorite, null) },
                text = { Text("Pledge Support") },
                containerColor = MaterialTheme.colorScheme.secondary)
        },
        snackbarHost = { if (opMsg != null) Snackbar(Modifier.padding(8.dp)) { Text(opMsg!!) } }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (need.beforePhotoUrl.isNotBlank()) item {
                Text("Current State", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(6.dp))
                AsyncImage(model = need.beforePhotoUrl, contentDescription = "Before",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)))
            }
            item { Text(need.description, fontSize = 15.sp, lineHeight = 22.sp) }
            item {
                Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Fund Progress", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { need.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${need.progressPercent}% collected", fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary)
                            Text("Target: ${fmt.format(need.targetAmount)}")
                        }
                        Text("Pledged: ${fmt.format(need.pledgedAmount)}", fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Donor Hall of Fame", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            if (pledges.isEmpty()) item {
                Text("Be the first to pledge!", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            } else items(pledges) { p -> DonorRow(p) }
        }
    }
    if (showPledgeDialog) PledgeDialog(onDismiss = { showPledgeDialog = false },
        onConfirm = { name, city, batch, amount ->
            vm.addPledge(Pledge(needId = needId, alumniName = name, alumniCity = city, batchYear = batch, amount = amount))
            showPledgeDialog = false
        })
}

@Composable
fun DonorRow(pledge: Pledge) {
    val fmt = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(pledge.timestamp))
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(pledge.alumniName, fontWeight = FontWeight.SemiBold)
                Text("${pledge.alumniCity} \u2022 Batch ${pledge.batchYear}", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(fmt.format(pledge.amount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Text(date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun PledgeDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var batch by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Pledge Your Support") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Your Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(city, { city = it }, label = { Text("Your City") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(batch, { batch = it }, label = { Text("Batch Year") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(amount, { amount = it }, label = { Text("Pledge Amount (\u20B9)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("\u26A0\uFE0F Simulated pledge. No actual payment collected.", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, city, batch, amount.toDoubleOrNull() ?: 0.0) },
                enabled = name.isNotBlank() && amount.toDoubleOrNull() != null) { Text("Confirm Pledge") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
