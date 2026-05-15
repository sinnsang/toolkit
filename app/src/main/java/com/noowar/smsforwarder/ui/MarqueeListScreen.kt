package com.noowar.smsforwarder.ui

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noowar.smsforwarder.R
import com.noowar.smsforwarder.data.AppDatabase
import com.noowar.smsforwarder.data.MarqueeItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ── Color constants shared with display screen ────────────────────────────────

val MARQUEE_RAINBOW = listOf(
    Color(0xFFFF0000), Color(0xFFFF8C00), Color(0xFFFFFF00),
    Color(0xFF00C000), Color(0xFF0000FF), Color(0xFF4B0082), Color(0xFF8B00FF)
)

val TEXT_COLOR_OPTIONS = listOf(
    "WHITE"   to Color.White,
    "RED"     to Color(0xFFFF0000),
    "ORANGE"  to Color(0xFFFF8C00),
    "YELLOW"  to Color(0xFFFFFF00),
    "GREEN"   to Color(0xFF00C000),
    "BLUE"    to Color(0xFF0000FF),
    "INDIGO"  to Color(0xFF4B0082),
    "VIOLET"  to Color(0xFF8B00FF),
    "RAINBOW" to Color.Unspecified
)

val STROKE_COLOR_OPTIONS = listOf(
    "NONE"   to Color.Unspecified,
    "BLACK"  to Color.Black,
    "WHITE"  to Color.White,
    "RED"    to Color(0xFFFF0000),
    "ORANGE" to Color(0xFFFF8C00),
    "YELLOW" to Color(0xFFFFFF00),
    "GREEN"  to Color(0xFF00C000),
    "BLUE"   to Color(0xFF0000FF),
    "INDIGO" to Color(0xFF4B0082),
    "VIOLET" to Color(0xFF8B00FF)
)

val BG_COLOR_OPTIONS = listOf(
    "BLACK"  to Color.Black,
    "WHITE"  to Color.White,
    "RED"    to Color(0xFFFF0000),
    "ORANGE" to Color(0xFFFF8C00),
    "YELLOW" to Color(0xFFFFFF00),
    "GREEN"  to Color(0xFF00C000),
    "BLUE"   to Color(0xFF0000FF),
    "INDIGO" to Color(0xFF4B0082),
    "VIOLET" to Color(0xFF8B00FF)
)

fun marqueeBgColor(key: String): Color =
    BG_COLOR_OPTIONS.find { it.first == key }?.second ?: Color.Black

fun marqueeTextColor(key: String): Color =
    TEXT_COLOR_OPTIONS.find { it.first == key }?.second ?: Color.White

fun marqueeStrokeColor(key: String): Color? =
    if (key == "NONE") null else STROKE_COLOR_OPTIONS.find { it.first == key }?.second

fun marqueeToFontFamily(fontType: String): FontFamily = when (fontType) {
    "SERIF" -> FontFamily.Serif
    "MONO"  -> FontFamily.Monospace
    else    -> FontFamily.SansSerif
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class MarqueeViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.getInstance(app).marqueeDao()

    val items = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(item: MarqueeItem) = viewModelScope.launch { dao.insert(item) }
    fun update(item: MarqueeItem) = viewModelScope.launch { dao.update(item) }
    fun delete(item: MarqueeItem) = viewModelScope.launch { dao.delete(item) }
}

// ── List screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarqueeListScreen(
    onBack: () -> Unit,
    onDisplay: (MarqueeItem) -> Unit,
    vm: MarqueeViewModel = viewModel()
) {
    val items by vm.items.collectAsState()
    var editing by remember { mutableStateOf<MarqueeItem?>(null) }
    var showEdit by remember { mutableStateOf(false) }

    if (showEdit) {
        MarqueeEditScreen(
            initial = editing,
            onBack = { showEdit = false },
            onSave = { item ->
                if (item.id == 0L) vm.add(item) else vm.update(item)
                showEdit = false
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_marquee)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showEdit = true }) {
                Icon(Icons.Default.Add, stringResource(R.string.title_add_marquee))
            }
        }
    ) { pad ->
        if (items.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(pad).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.no_marquees), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.no_marquees_hint), style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    MarqueeCard(
                        item = item,
                        onPlay = { onDisplay(item) },
                        onEdit = { editing = item; showEdit = true },
                        onDelete = { vm.delete(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MarqueeCard(
    item: MarqueeItem,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(marqueeBgColor(item.bgColorKey), shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.text.take(3),
                    color = if (item.textColorKey == "RAINBOW") MARQUEE_RAINBOW[0]
                            else marqueeTextColor(item.textColorKey),
                    fontSize = 14.sp,
                    fontFamily = marqueeToFontFamily(item.fontType),
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.direction} · ${item.fontType} · ${item.fontSize}sp · ×${item.speed}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onPlay) {
                Icon(Icons.Default.PlayArrow, "Play")
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, stringResource(R.string.cd_edit))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, stringResource(R.string.cd_delete))
            }
        }
    }
}

// ── Edit screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarqueeEditScreen(
    initial: MarqueeItem?,
    onBack: () -> Unit,
    onSave: (MarqueeItem) -> Unit
) {
    var text       by remember { mutableStateOf(initial?.text ?: "") }
    var fontType   by remember { mutableStateOf(initial?.fontType ?: "SANS") }
    var fontSize   by remember { mutableStateOf((initial?.fontSize ?: 60).toFloat()) }
    var direction  by remember { mutableStateOf(initial?.direction ?: "LEFT") }
    var textColor  by remember { mutableStateOf(initial?.textColorKey ?: "WHITE") }
    var strokeColor by remember { mutableStateOf(initial?.strokeColorKey ?: "NONE") }
    var speed      by remember { mutableStateOf((initial?.speed ?: 3).toFloat()) }
    var tts        by remember { mutableStateOf(initial?.ttsEnabled ?: false) }
    var bgColor    by remember { mutableStateOf(initial?.bgColorKey ?: "BLACK") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (initial == null) R.string.title_add_marquee else R.string.title_edit_marquee))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Text input ─────────────────────────────────────────────────
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.label_marquee_text)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // ── Font type ──────────────────────────────────────────────────
            EditSection(stringResource(R.string.section_font)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SANS" to R.string.font_sans, "SERIF" to R.string.font_serif, "MONO" to R.string.font_mono)
                        .forEach { (key, res) ->
                            ToggleChip(stringResource(res), fontType == key) { fontType = key }
                        }
                }
            }

            // ── Font size ──────────────────────────────────────────────────
            EditSection(stringResource(R.string.label_font_size, fontSize.toInt())) {
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 20f..120f,
                    steps = 24,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Direction ──────────────────────────────────────────────────
            EditSection(stringResource(R.string.section_direction)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("LEFT" to "←", "RIGHT" to "→", "UP" to "↑", "DOWN" to "↓", "BOUNCE" to "↔")
                        .forEach { (key, label) ->
                            ToggleChip(label, direction == key) { direction = key }
                        }
                }
            }

            // ── Text color ─────────────────────────────────────────────────
            EditSection(stringResource(R.string.section_text_color)) {
                ColorPickerRow(TEXT_COLOR_OPTIONS, textColor) { textColor = it }
            }

            // ── Stroke color ───────────────────────────────────────────────
            EditSection(stringResource(R.string.section_stroke_color)) {
                ColorPickerRow(STROKE_COLOR_OPTIONS, strokeColor) { strokeColor = it }
            }

            // ── Speed ──────────────────────────────────────────────────────
            EditSection(stringResource(R.string.label_speed, speed.toInt())) {
                Slider(
                    value = speed,
                    onValueChange = { speed = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── TTS ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.label_tts), style = MaterialTheme.typography.bodyMedium)
                Switch(checked = tts, onCheckedChange = { tts = it })
            }

            // ── Background color ───────────────────────────────────────────
            EditSection(stringResource(R.string.section_bg_color)) {
                ColorPickerRow(BG_COLOR_OPTIONS, bgColor) { bgColor = it }
            }

            // ── Buttons ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.btn_cancel))
                }
                Button(
                    onClick = {
                        onSave(MarqueeItem(
                            id = initial?.id ?: 0,
                            text = text.trim(),
                            fontType = fontType,
                            fontSize = fontSize.toInt(),
                            direction = direction,
                            textColorKey = textColor,
                            strokeColorKey = strokeColor,
                            speed = speed.toInt(),
                            ttsEnabled = tts,
                            bgColorKey = bgColor
                        ))
                    },
                    modifier = Modifier.weight(1f),
                    enabled = text.isNotBlank()
                ) {
                    Text(stringResource(R.string.btn_save))
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Shared small composables ──────────────────────────────────────────────────

@Composable
private fun EditSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

@Composable
private fun ToggleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
            Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
            Text(label)
        }
    }
}

@Composable
private fun ColorPickerRow(
    options: List<Pair<String, Color>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options) { (key, color) ->
            ColorDot(
                key = key,
                color = color,
                selected = selected == key,
                onClick = { onSelect(key) }
            )
        }
    }
}

@Composable
private fun ColorDot(key: String, color: Color, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
    val borderWidth = if (selected) 3.dp else 1.dp

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .border(borderWidth, borderColor, CircleShape)
            .then(
                when (key) {
                    "NONE"    -> Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    "RAINBOW" -> Modifier.background(Color.LightGray, CircleShape)
                    else      -> Modifier.background(color, CircleShape)
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when (key) {
            "NONE"    -> Text("✕", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            "RAINBOW" -> Text("🌈", fontSize = 16.sp)
        }
    }
}
