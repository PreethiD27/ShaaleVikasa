package com.shaalevikas.app.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shaalevikas.app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repo = FirebaseRepository()

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()
    val isAdmin get() = _userRole.value?.role == "admin"

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun observeUserRole() {
        viewModelScope.launch { repo.currentUserRole().collect { _userRole.value = it } }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.login(email, password)
                .onSuccess { _userRole.value = it; onSuccess() }
                .onFailure { _authError.value = it.message }
        }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repo.register(email, password)
                .onSuccess { _userRole.value = it; onSuccess() }
                .onFailure { _authError.value = it.message }
        }
    }

    fun logout(onDone: () -> Unit) { repo.logout(); _userRole.value = null; onDone() }
    fun clearAuthError() { _authError.value = null }

    val needs: StateFlow<List<Need>> = repo.getNeedsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _filterCategory = MutableStateFlow("All")
    val filterCategory: StateFlow<String> = _filterCategory.asStateFlow()
    private val _filterStatus = MutableStateFlow("All")
    val filterStatus: StateFlow<String> = _filterStatus.asStateFlow()

    val filteredNeeds: StateFlow<List<Need>> =
        combine(needs, _searchQuery, _filterCategory, _filterStatus) { list, q, cat, status ->
            list.filter { need ->
                val mQ = q.isBlank() || need.title.contains(q, true) || need.description.contains(q, true)
                val mC = cat == "All" || need.category == cat
                val mS = status == "All" || need.status == status
                mQ && mC && mS
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearch(q: String) { _searchQuery.value = q }
    fun setFilterCategory(c: String) { _filterCategory.value = c }
    fun setFilterStatus(s: String) { _filterStatus.value = s }

    private val _opMessage = MutableStateFlow<String?>(null)
    val opMessage: StateFlow<String?> = _opMessage.asStateFlow()

    fun addNeed(need: Need) {
        viewModelScope.launch {
            runCatching { repo.addNeed(need) }
                .onSuccess { _opMessage.value = "Need added!" }
                .onFailure { _opMessage.value = "Error: ${it.message}" }
        }
    }

    fun updateNeed(need: Need) {
        viewModelScope.launch {
            runCatching { repo.updateNeed(need) }
                .onSuccess { _opMessage.value = "Need updated!" }
                .onFailure { _opMessage.value = "Error: ${it.message}" }
        }
    }

    fun deleteNeed(needId: String) {
        viewModelScope.launch {
            runCatching { repo.deleteNeed(needId) }
                .onSuccess { _opMessage.value = "Need deleted." }
                .onFailure { _opMessage.value = "Error: ${it.message}" }
        }
    }

    fun markFulfilled(need: Need, afterUri: Uri?) {
        viewModelScope.launch {
            var afterUrl = need.afterPhotoUrl
            if (afterUri != null)
                afterUrl = repo.uploadPhoto(afterUri, "after/${need.id}_${System.currentTimeMillis()}.jpg")
            repo.updateNeed(need.copy(status = "Fulfilled", afterPhotoUrl = afterUrl))
            _opMessage.value = "Marked as fulfilled!"
        }
    }

    fun uploadBeforePhoto(needId: String, uri: Uri, onUrl: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { repo.uploadPhoto(uri, "before/${needId}_${System.currentTimeMillis()}.jpg") }
                .onSuccess { onUrl(it) }
                .onFailure { _opMessage.value = "Upload failed: ${it.message}" }
        }
    }

    fun clearOpMessage() { _opMessage.value = null }

    private val _currentNeedId = MutableStateFlow("")
    val pledges: StateFlow<List<Pledge>> = _currentNeedId
        .flatMapLatest { id -> if (id.isBlank()) flowOf(emptyList()) else repo.getPledgesFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPledgesFor(needId: String) { _currentNeedId.value = needId }

    fun addPledge(pledge: Pledge) {
        viewModelScope.launch {
            runCatching { repo.addPledge(pledge) }
                .onSuccess { _opMessage.value = "Pledge recorded! Thank you, ${pledge.alumniName}!" }
                .onFailure { _opMessage.value = "Error: ${it.message}" }
        }
    }

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()
    private val _aiResult = MutableStateFlow<String?>(null)
    val aiResult: StateFlow<String?> = _aiResult.asStateFlow()

    fun generateDescription(prompt: String) {
        viewModelScope.launch {
            _aiLoading.value = true
            AnthropicService.generateNeedDescription(prompt)
                .onSuccess { _aiResult.value = it }
                .onFailure { _opMessage.value = "AI error: ${it.message}" }
            _aiLoading.value = false
        }
    }

    fun generateImpactSummary(title: String, desc: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _aiLoading.value = true
            AnthropicService.generateImpactSummary(title, desc)
                .onSuccess { onResult(it) }
                .onFailure { _opMessage.value = "AI error: ${it.message}" }
            _aiLoading.value = false
        }
    }

    fun clearAiResult() { _aiResult.value = null }
}
