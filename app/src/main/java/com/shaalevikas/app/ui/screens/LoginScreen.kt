package com.shaalevikas.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaalevikas.app.ui.MainViewModel

@Composable
fun LoginScreen(vm: MainViewModel, onSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val authError by vm.authError.collectAsState()

    Column(Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("\uD83C\uDFEB Shaale-Vikas", fontSize = 28.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text("School-Alumni Bridge", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(40.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") },
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") },
            singleLine = true, visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        if (authError != null) Text(authError!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Button(onClick = { loading = true; vm.clearAuthError()
            vm.login(email, password) { loading = false; onSuccess() } },
            enabled = email.isNotBlank() && password.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth().height(50.dp)) {
            if (loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
            else Text("Login")
        }
    }
}

@Composable
fun RegisterScreen(vm: MainViewModel, onSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val authError by vm.authError.collectAsState()

    Column(Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Join Shaale-Vikas", fontSize = 24.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") },
            singleLine = true, visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        if (authError != null) Text(authError!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        Button(onClick = { loading = true; vm.clearAuthError()
            vm.register(email, password) { loading = false; onSuccess() } },
            enabled = email.isNotBlank() && password.length >= 6 && !loading,
            modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Register as Alumni") }
    }
}
