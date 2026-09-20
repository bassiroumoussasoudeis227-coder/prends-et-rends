package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AndroidAudioPlayerManager
import com.example.audio.AndroidAudioRecorderManager
import com.example.audio.AudioPlaybackState
import com.example.audio.AudioPlayerManager
import com.example.audio.AudioRecorderManager
import com.example.data.LoansRepository
import com.example.location.DeviceLocationManager
import com.example.location.LocationResultData
import com.example.model.AppLanguage
import com.example.model.LoanCategory
import com.example.model.LoanItem
import com.example.model.LoanStatus
import com.example.model.LoanType
import com.example.model.LocalizationManager
import com.example.model.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class FilterTab(val labelKey: String) {
    ALL("tab_all"),
    LENT("tab_lent"),
    BORROWED("tab_borrowed"),
    OVERDUE("tab_overdue"),
    RETURNED("tab_returned")
}

data class LoansUiState(
    val items: List<LoanItem> = emptyList(),
    val totalLent: Int = 0,
    val totalBorrowed: Int = 0,
    val totalOverdue: Int = 0,
    val totalReturned: Int = 0,
    val isSyncing: Boolean = false,
    val currentLanguage: AppLanguage = AppLanguage.FRENCH
)

data class FilterCriteria(
    val query: String,
    val filter: FilterTab,
    val category: LoanCategory?,
    val lang: AppLanguage
)

class LoansViewModel(
    application: Application,
    private val repository: LoansRepository = LoansRepository()
) : AndroidViewModel(application) {

    private val locationManager = DeviceLocationManager(application)
    val audioPlayer: AudioPlayerManager = AndroidAudioPlayerManager(application)
    val audioRecorder: AudioRecorderManager = AndroidAudioRecorderManager(application)

    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow(FilterTab.ALL)
    val selectedCategory = MutableStateFlow<LoanCategory?>(null)

    private val _isCapturingLocation = MutableStateFlow(false)
    val isCapturingLocation: StateFlow<Boolean> = _isCapturingLocation.asStateFlow()

    private val _capturedLocation = MutableStateFlow<LocationResultData?>(null)
    val capturedLocation: StateFlow<LocationResultData?> = _capturedLocation.asStateFlow()

    private val _attachedMedia = MutableStateFlow<List<MediaItem>>(emptyList())
    val attachedMedia: StateFlow<List<MediaItem>> = _attachedMedia.asStateFlow()

    private val _recordedAudioPath = MutableStateFlow<String?>(null)
    val recordedAudioPath: StateFlow<String?> = _recordedAudioPath.asStateFlow()

    private val filterCriteria = combine(
        searchQuery,
        selectedFilter,
        selectedCategory,
        LocalizationManager.currentLanguage
    ) { query, filter, category, lang ->
        FilterCriteria(query, filter, category, lang)
    }

    val uiState: StateFlow<LoansUiState> = combine(
        repository.loans,
        filterCriteria,
        repository.isSyncing
    ) { loans, criteria, syncing ->
        val query = criteria.query
        val filter = criteria.filter
        val category = criteria.category
        val lang = criteria.lang

        val totalLent = loans.count { it.type == LoanType.LENT && it.status == LoanStatus.ACTIVE }
        val totalBorrowed = loans.count { it.type == LoanType.BORROWED && it.status == LoanStatus.ACTIVE }
        val totalOverdue = loans.count { it.isOverdue }
        val totalReturned = loans.count { it.status == LoanStatus.RETURNED }

        val filtered = loans.filter { item ->
            // Search
            val matchesQuery = query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                item.personName.contains(query, ignoreCase = true) ||
                item.notes.contains(query, ignoreCase = true) ||
                (item.address?.contains(query, ignoreCase = true) == true)

            // Category filter
            val matchesCategory = category == null || item.category == category

            // Tab filter
            val matchesTab = when (filter) {
                FilterTab.ALL -> true
                FilterTab.LENT -> item.type == LoanType.LENT && item.status == LoanStatus.ACTIVE
                FilterTab.BORROWED -> item.type == LoanType.BORROWED && item.status == LoanStatus.ACTIVE
                FilterTab.OVERDUE -> item.isOverdue
                FilterTab.RETURNED -> item.status == LoanStatus.RETURNED
            }

            matchesQuery && matchesCategory && matchesTab
        }

        LoansUiState(
            items = filtered,
            totalLent = totalLent,
            totalBorrowed = totalBorrowed,
            totalOverdue = totalOverdue,
            totalReturned = totalReturned,
            isSyncing = syncing,
            currentLanguage = lang
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LoansUiState()
    )

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun onFilterSelected(filter: FilterTab) {
        selectedFilter.value = filter
    }

    fun onCategorySelected(category: LoanCategory?) {
        selectedCategory.value = if (selectedCategory.value == category) null else category
    }

    fun setLanguage(language: AppLanguage) {
        LocalizationManager.setLanguage(language)
    }

    // --- Location Actions ---
    fun captureCurrentLocation() {
        viewModelScope.launch {
            _isCapturingLocation.value = true
            try {
                val loc = locationManager.capturePreciseLocation()
                _capturedLocation.value = loc
            } finally {
                _isCapturingLocation.value = false
            }
        }
    }

    fun clearCapturedLocation() {
        _capturedLocation.value = null
    }

    // --- Media Attachments Actions ---
    fun addMediaAttachment(item: MediaItem) {
        _attachedMedia.value = _attachedMedia.value + item
    }

    fun removeMediaAttachment(mediaId: String) {
        _attachedMedia.value = _attachedMedia.value.filter { it.id != mediaId }
    }

    fun clearAttachedMedia() {
        _attachedMedia.value = emptyList()
    }

    // --- Audio Voice Note Actions ---
    fun startVoiceRecording() {
        val app = getApplication<Application>()
        val audioFile = File(app.filesDir, "voice_note_${System.currentTimeMillis()}.m4a")
        audioRecorder.startRecording(audioFile)
    }

    fun stopVoiceRecording() {
        val path = audioRecorder.stopRecording()
        if (path != null) {
            _recordedAudioPath.value = path
        }
    }

    fun cancelVoiceRecording() {
        audioRecorder.cancelRecording()
    }

    fun deleteRecordedAudio() {
        val path = _recordedAudioPath.value
        if (path != null) {
            try {
                File(path).delete()
            } catch (_: Exception) {}
        }
        _recordedAudioPath.value = null
        audioPlayer.stop()
    }

    fun playAudio(path: String) {
        audioPlayer.play(path)
    }

    fun pauseAudio() {
        audioPlayer.pause()
    }

    fun toggleLoanStatus(loanId: String) {
        viewModelScope.launch {
            repository.toggleStatus(loanId)
        }
    }

    fun deleteLoan(loanId: String) {
        viewModelScope.launch {
            repository.deleteLoan(loanId)
        }
    }

    fun saveLoan(
        title: String,
        personName: String,
        type: LoanType,
        category: LoanCategory,
        dueDate: Long?,
        notes: String
    ) {
        viewModelScope.launch {
            val loc = _capturedLocation.value
            val newItem = LoanItem(
                title = title.trim(),
                personName = personName.trim(),
                type = type,
                category = category,
                startDate = System.currentTimeMillis(),
                dueDate = dueDate,
                notes = notes.trim(),
                status = LoanStatus.ACTIVE,
                latitude = loc?.latitude,
                longitude = loc?.longitude,
                address = loc?.address,
                audioPath = _recordedAudioPath.value,
                mediaAttachments = _attachedMedia.value
            )
            repository.addLoan(newItem)

            // Reset draft states
            _capturedLocation.value = null
            _attachedMedia.value = emptyList()
            _recordedAudioPath.value = null
        }
    }

    fun resetDraft() {
        _capturedLocation.value = null
        _attachedMedia.value = emptyList()
        _recordedAudioPath.value = null
    }

    fun refresh() {
        repository.syncWithFirebase()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
