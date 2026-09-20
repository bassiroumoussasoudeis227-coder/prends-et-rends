package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.AudioPlaybackState
import com.example.model.AppLanguage
import com.example.model.LoanCategory
import com.example.model.LoanItem
import com.example.model.LoanStatus
import com.example.model.LoanType
import com.example.model.LocalizationManager
import com.example.model.MediaItem
import com.example.model.MediaType
import com.example.ui.components.AudioPlayerWidget
import com.example.ui.components.MediaCarousel
import com.example.ui.components.SatelliteMapWidget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    viewModel: LoansViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val playbackState by viewModel.audioPlayer.playbackState.collectAsState()
    val currentAudioPlaying by viewModel.audioPlayer.currentPlayingPath.collectAsState()
    val context = LocalContext.current

    var showAddSheet by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = LocalizationManager.string("app_title"),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Cloud status badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (uiState.isSyncing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                                        contentDescription = "Sync",
                                        tint = if (uiState.isSyncing) MaterialTheme.colorScheme.primary else Color(0xFF0F9D58),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (uiState.isSyncing) LocalizationManager.string("cloud_syncing") else LocalizationManager.string("cloud_synced"),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                        Text(
                            text = LocalizationManager.string("app_subtitle"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Language Switcher Button
                    Box {
                        IconButton(
                            onClick = { showLanguageMenu = true },
                            modifier = Modifier.testTag("language_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.currentLanguage.flagEmoji,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            AppLanguage.values().forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${lang.flagEmoji}  ${lang.displayName}")
                                    },
                                    onClick = {
                                        viewModel.setLanguage(lang)
                                        showLanguageMenu = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.resetDraft()
                    showAddSheet = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text(LocalizationManager.string("new_loan")) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_loan_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // KPI Summary Row
            KpiSummaryRow(uiState = uiState)

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("search_field"),
                placeholder = {
                    Text(
                        LocalizationManager.string("search_placeholder"),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedFilter.ordinal,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterTab.values().forEach { tab ->
                    val tabText = when (tab) {
                        FilterTab.ALL -> "${LocalizationManager.string(tab.labelKey)} (${uiState.items.size})"
                        FilterTab.LENT -> "${LocalizationManager.string(tab.labelKey)} (${uiState.totalLent})"
                        FilterTab.BORROWED -> "${LocalizationManager.string(tab.labelKey)} (${uiState.totalBorrowed})"
                        FilterTab.OVERDUE -> "${LocalizationManager.string(tab.labelKey)} (${uiState.totalOverdue})"
                        FilterTab.RETURNED -> "${LocalizationManager.string(tab.labelKey)} (${uiState.totalReturned})"
                    }
                    Tab(
                        selected = selectedFilter == tab,
                        onClick = { viewModel.onFilterSelected(tab) },
                        text = {
                            Text(
                                text = tabText,
                                fontWeight = if (selectedFilter == tab) FontWeight.Bold else FontWeight.Normal,
                                color = if (tab == FilterTab.OVERDUE && uiState.totalOverdue > 0)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            // Category Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.onCategorySelected(null) },
                        label = { Text(LocalizationManager.string("cat_all")) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                items(LoanCategory.values()) { cat ->
                    val catLabel = when (cat) {
                        LoanCategory.TOOLS -> LocalizationManager.string("cat_tools")
                        LoanCategory.BOOKS -> LocalizationManager.string("cat_books")
                        LoanCategory.HIGH_TECH -> LocalizationManager.string("cat_high_tech")
                        LoanCategory.MONEY -> LocalizationManager.string("cat_money")
                        LoanCategory.GAMES -> LocalizationManager.string("cat_games")
                        LoanCategory.CLOTHES -> LocalizationManager.string("cat_clothes")
                        LoanCategory.OTHER -> LocalizationManager.string("cat_other")
                    }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { viewModel.onCategorySelected(cat) },
                        label = { Text(catLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Loan Cards List
            if (uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val emptyMessage = when (selectedFilter) {
                        FilterTab.ALL -> LocalizationManager.string("empty_all")
                        FilterTab.LENT -> LocalizationManager.string("empty_lent")
                        FilterTab.BORROWED -> LocalizationManager.string("empty_borrowed")
                        FilterTab.OVERDUE -> LocalizationManager.string("empty_overdue")
                        FilterTab.RETURNED -> LocalizationManager.string("empty_returned")
                    }
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        LoanCard(
                            item = item,
                            playbackState = playbackState,
                            isPlayingThis = currentAudioPlaying == item.audioPath,
                            onToggleStatus = { viewModel.toggleLoanStatus(item.id) },
                            onDelete = { viewModel.deleteLoan(item.id) },
                            onPlayAudio = { path -> viewModel.playAudio(path) },
                            onPauseAudio = { viewModel.pauseAudio() },
                            onShare = { shareLoan(context, item) }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddLoanBottomSheet(
            viewModel = viewModel,
            onDismiss = { showAddSheet = false }
        )
    }
}

@Composable
fun KpiSummaryRow(uiState: LoansUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KpiBadge(
            count = uiState.totalLent,
            label = LocalizationManager.string("kpi_lent"),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f)
        )
        KpiBadge(
            count = uiState.totalBorrowed,
            label = LocalizationManager.string("kpi_borrowed"),
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f)
        )
        KpiBadge(
            count = uiState.totalOverdue,
            label = LocalizationManager.string("kpi_overdue"),
            backgroundColor = if (uiState.totalOverdue > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
            textColor = if (uiState.totalOverdue > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        KpiBadge(
            count = uiState.totalReturned,
            label = LocalizationManager.string("kpi_returned"),
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun KpiBadge(
    count: Int,
    label: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LoanCard(
    item: LoanItem,
    playbackState: AudioPlaybackState,
    isPlayingThis: Boolean,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    onPlayAudio: (String) -> Unit,
    onPauseAudio: () -> Unit,
    onShare: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val isReturned = item.status == LoanStatus.RETURNED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("loan_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReturned) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isReturned) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Type Badge + Category + Overdue alert
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Type Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (item.type == LoanType.LENT) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (item.type == LoanType.LENT) LocalizationManager.string("tab_lent") else LocalizationManager.string("tab_borrowed"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.type == LoanType.LENT) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Category text
                    Text(
                        text = item.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (item.isOverdue) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Overdue",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = LocalizationManager.string("overdue_badge"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isReturned) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (isReturned) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )

            // Person Name
            Text(
                text = "${if (item.type == LoanType.LENT) LocalizationManager.string("lent_to") else LocalizationManager.string("borrowed_from")} ${item.personName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Due date / days remaining
            if (item.dueDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val dueText = dateFormat.format(Date(item.dueDate))
                val remaining = item.daysRemaining
                val remainingLabel = when {
                    isReturned -> ""
                    remaining != null && remaining < 0 -> " (${-remaining}j de retard)"
                    remaining != null && remaining == 0L -> " (Aujourd'hui !)"
                    remaining != null -> " (dans ${remaining}j)"
                    else -> ""
                }
                Text(
                    text = "${LocalizationManager.string("due_on")} $dueText$remainingLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (item.isOverdue) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Notes
            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Satellite Map View (GPS feature)
            if (item.hasCoordinates) {
                Spacer(modifier = Modifier.height(10.dp))
                SatelliteMapWidget(
                    latitude = item.latitude!!,
                    longitude = item.longitude!!,
                    address = item.address,
                    personName = item.personName
                )
            }

            // Media Attachments carousel
            if (item.mediaAttachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                MediaCarousel(
                    attachments = item.mediaAttachments
                )
            }

            // Voice Note Audio Player
            if (item.audioPath != null) {
                Spacer(modifier = Modifier.height(10.dp))
                AudioPlayerWidget(
                    audioPath = item.audioPath,
                    playbackState = playbackState,
                    isPlayingThis = isPlayingThis,
                    onPlay = { onPlayAudio(item.audioPath) },
                    onPause = onPauseAudio
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Toggle returned button
                    Button(
                        onClick = onToggleStatus,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReturned) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isReturned) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("toggle_status_button_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (isReturned) Icons.Default.Refresh else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isReturned) LocalizationManager.string("mark_active") else LocalizationManager.string("mark_returned"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Share reminder
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanBottomSheet(
    viewModel: LoansViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var personName by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(LoanType.LENT) }
    var category by remember { mutableStateOf(LoanCategory.OTHER) }
    var selectedDelayDays by remember { mutableStateOf<Int?>(14) }
    var notes by remember { mutableStateOf("") }

    val isCapturingLocation by viewModel.isCapturingLocation.collectAsState()
    val capturedLocation by viewModel.capturedLocation.collectAsState()
    val attachedMedia by viewModel.attachedMedia.collectAsState()
    val recordedAudioPath by viewModel.recordedAudioPath.collectAsState()
    val isRecordingAudio by viewModel.audioRecorder.isRecording.collectAsState()
    val playbackState by viewModel.audioPlayer.playbackState.collectAsState()
    val currentAudioPlaying by viewModel.audioPlayer.currentPlayingPath.collectAsState()

    // Permission launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.captureCurrentLocation()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startVoiceRecording()
        }
    }

    // Media Picker
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        uris.forEach { uri ->
            viewModel.addMediaAttachment(
                MediaItem(
                    uri = uri.toString(),
                    type = MediaType.PHOTO
                )
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = LocalizationManager.string("sheet_title"),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Operation Type: Lent vs Borrowed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { type = LoanType.LENT },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == LoanType.LENT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (type == LoanType.LENT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(LocalizationManager.string("tab_lent"), fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { type = LoanType.BORROWED },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == LoanType.BORROWED) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (type == LoanType.BORROWED) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(LocalizationManager.string("tab_borrowed"), fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Item Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(LocalizationManager.string("item_title")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loan_title_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Person Name
            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                label = {
                    Text(
                        if (type == LoanType.LENT) LocalizationManager.string("person_name_lent")
                        else LocalizationManager.string("person_name_borrowed")
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loan_person_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips
            Text(
                text = LocalizationManager.string("category"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(LoanCategory.values()) { cat ->
                    val catLabel = when (cat) {
                        LoanCategory.TOOLS -> LocalizationManager.string("cat_tools")
                        LoanCategory.BOOKS -> LocalizationManager.string("cat_books")
                        LoanCategory.HIGH_TECH -> LocalizationManager.string("cat_high_tech")
                        LoanCategory.MONEY -> LocalizationManager.string("cat_money")
                        LoanCategory.GAMES -> LocalizationManager.string("cat_games")
                        LoanCategory.CLOTHES -> LocalizationManager.string("cat_clothes")
                        LoanCategory.OTHER -> LocalizationManager.string("cat_other")
                    }
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(catLabel, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Due Date selector
            Text(
                text = LocalizationManager.string("due_date_delay"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    7 to LocalizationManager.string("delay_7_days"),
                    14 to LocalizationManager.string("delay_14_days"),
                    30 to LocalizationManager.string("delay_30_days"),
                    null to LocalizationManager.string("delay_none")
                ).forEach { (days, label) ->
                    FilterChip(
                        selected = selectedDelayDays == days,
                        onClick = { selectedDelayDays = days },
                        label = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- FEATURE 1: SATELLITE LOCATION TRACKING ---
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF1F5F9),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = "GPS",
                                tint = Color(0xFF0F9D58),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = LocalizationManager.string("satellite_tracking"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )
                        }

                        if (capturedLocation != null) {
                            IconButton(
                                onClick = { viewModel.clearCapturedLocation() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear location", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (capturedLocation != null) {
                        Text(
                            text = capturedLocation?.address ?: "${capturedLocation?.latitude}, ${capturedLocation?.longitude}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "GPS: ${capturedLocation?.latitude}°N, ${capturedLocation?.longitude}°E",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    } else {
                        Button(
                            onClick = {
                                val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (fineGranted) {
                                    viewModel.captureCurrentLocation()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            enabled = !isCapturingLocation,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0F9D58),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isCapturingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(LocalizationManager.string("capturing_gps"), fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(LocalizationManager.string("capture_gps"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- FEATURE 2 & 3: AUDIO MEMO & MEDIA ATTACHMENTS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Voice Note Button
                Button(
                    onClick = {
                        if (isRecordingAudio) {
                            viewModel.stopVoiceRecording()
                        } else {
                            val micGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            if (micGranted) {
                                viewModel.startVoiceRecording()
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecordingAudio) Color(0xFFE11D48) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isRecordingAudio) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isRecordingAudio) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Mic",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRecordingAudio) LocalizationManager.string("stop_recording") else LocalizationManager.string("voice_note"),
                        fontSize = 12.sp
                    )
                }

                // Add Media Button (Gallery / Camera)
                Button(
                    onClick = {
                        mediaPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Media", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(LocalizationManager.string("pick_media"), fontSize = 12.sp)
                }
            }

            // Audio Player preview if recorded
            if (recordedAudioPath != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AudioPlayerWidget(
                    audioPath = recordedAudioPath!!,
                    playbackState = playbackState,
                    isPlayingThis = currentAudioPlaying == recordedAudioPath,
                    onPlay = { viewModel.playAudio(recordedAudioPath!!) },
                    onPause = { viewModel.pauseAudio() },
                    onDelete = { viewModel.deleteRecordedAudio() }
                )
            }

            // Attached Media preview carousel
            if (attachedMedia.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                MediaCarousel(
                    attachments = attachedMedia,
                    onRemoveAttachment = { mediaId -> viewModel.removeMediaAttachment(mediaId) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(LocalizationManager.string("notes_placeholder")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loan_notes_input"),
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    if (title.isNotBlank() && personName.isNotBlank()) {
                        val oneDay = 24 * 60 * 60 * 1000L
                        val dueDate = selectedDelayDays?.let { System.currentTimeMillis() + it * oneDay }
                        viewModel.saveLoan(
                            title = title,
                            personName = personName,
                            type = type,
                            category = category,
                            dueDate = dueDate,
                            notes = notes
                        )
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank() && personName.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_loan_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = LocalizationManager.string("save"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun shareLoan(context: Context, item: LoanItem) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dueStr = item.dueDate?.let { dateFormat.format(Date(it)) } ?: "dès que possible"
    val message = String.format(
        LocalizationManager.string("reminder_message_template"),
        item.personName,
        item.title,
        dueStr
    )
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Rappel de prêt")
    context.startActivity(shareIntent)
}
