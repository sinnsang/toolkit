package com.noowar.smsforwarder.ui

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noowar.smsforwarder.R
import com.noowar.smsforwarder.data.ForwardRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditScreen(
    initial: ForwardRule?,
    onBack: () -> Unit,
    viewModel: RuleViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isNew = initial == null

    var senderFilter by remember { mutableStateOf(initial?.senderFilter ?: "") }
    var bodyFilter by remember { mutableStateOf(initial?.bodyFilter ?: "") }
    var matchMode by remember { mutableStateOf(initial?.matchMode ?: "OR") }
    var destination by remember { mutableStateOf(initial?.destination ?: "") }
    var messageFormat by remember { mutableStateOf(initial?.messageFormat ?: "ORIGINAL") }
    var formatString by remember { mutableStateOf(initial?.formatString ?: "") }
    var channelType by remember { mutableStateOf(initial?.channelType ?: "SMS") }

    val senderContactLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { senderFilter = resolvePhoneNumber(context, it) ?: senderFilter }
        focusManager.clearFocus()
    }

    val destContactLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { destination = resolvePhoneNumber(context, it) ?: destination }
        focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isNew) R.string.title_add_rule else R.string.title_edit_rule)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SectionTitle(stringResource(R.string.section_filter))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = senderFilter,
                    onValueChange = { senderFilter = it },
                    label = { Text(stringResource(R.string.label_sender_filter)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedButton(onClick = { senderContactLauncher.launch(null) }) {
                    Text(stringResource(R.string.btn_contacts))
                }
            }

            OutlinedTextField(
                value = bodyFilter,
                onValueChange = { bodyFilter = it },
                label = { Text(stringResource(R.string.label_keyword_filter)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.label_match_prefix), style = MaterialTheme.typography.bodyMedium)
                ToggleButton(
                    label = "OR",
                    selected = matchMode == "OR",
                    onClick = { matchMode = "OR" }
                )
                ToggleButton(
                    label = "AND",
                    selected = matchMode == "AND",
                    onClick = { matchMode = "AND" }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            SectionTitle(stringResource(R.string.section_forward))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.label_channel), style = MaterialTheme.typography.bodyMedium)
                ToggleButton(
                    label = "SMS",
                    selected = channelType == "SMS",
                    onClick = { channelType = "SMS" }
                )
                ToggleButton(
                    label = stringResource(R.string.label_telegram),
                    selected = channelType == "TELEGRAM",
                    onClick = { channelType = "TELEGRAM" }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text(stringResource(if (channelType == "TELEGRAM") R.string.label_chat_id else R.string.label_phone_number)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                if (channelType == "SMS") {
                    OutlinedButton(onClick = { destContactLauncher.launch(null) }) {
                        Text(stringResource(R.string.btn_contacts))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            SectionTitle(stringResource(R.string.section_format))

            FormatOption(
                label = stringResource(R.string.format_original_label),
                selected = messageFormat == "ORIGINAL",
                onClick = { messageFormat = "ORIGINAL" }
            )
            FormatOption(
                label = stringResource(R.string.format_prepend_label),
                selected = messageFormat == "PREPEND",
                onClick = { messageFormat = "PREPEND" }
            )
            if (messageFormat == "PREPEND") {
                OutlinedTextField(
                    value = formatString,
                    onValueChange = { formatString = it },
                    label = { Text(stringResource(R.string.label_prepend_string)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp)
                )
            }
            FormatOption(
                label = stringResource(R.string.format_append_label),
                selected = messageFormat == "APPEND",
                onClick = { messageFormat = "APPEND" }
            )
            if (messageFormat == "APPEND") {
                OutlinedTextField(
                    value = formatString,
                    onValueChange = { formatString = it },
                    label = { Text(stringResource(R.string.label_append_string)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp)
                )
            }
            FormatOption(
                label = stringResource(R.string.format_replace_label),
                selected = messageFormat == "REPLACE",
                onClick = { messageFormat = "REPLACE" }
            )
            if (messageFormat == "REPLACE") {
                OutlinedTextField(
                    value = formatString,
                    onValueChange = { formatString = it },
                    label = { Text(stringResource(R.string.label_replace_string)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.btn_cancel))
                }
                Button(
                    onClick = {
                        val rule = ForwardRule(
                            id = initial?.id ?: 0,
                            isEnabled = initial?.isEnabled ?: true,
                            senderFilter = senderFilter.trim(),
                            bodyFilter = bodyFilter.trim(),
                            matchMode = matchMode,
                            destination = destination.trim(),
                            messageFormat = messageFormat,
                            formatString = formatString,
                            channelType = channelType
                        )
                        if (isNew) viewModel.addRule(rule) else viewModel.updateRule(rule)
                        onBack()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.btn_save))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun FormatOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ToggleButton(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick) { Text(label) }
    }
}

fun resolvePhoneNumber(context: Context, contactUri: Uri): String? {
    val contactId = contactUri.lastPathSegment ?: return null
    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
        arrayOf(contactId),
        null
    )
    return cursor?.use { if (it.moveToFirst()) it.getString(0)?.filter(Char::isDigit) else null }
}
