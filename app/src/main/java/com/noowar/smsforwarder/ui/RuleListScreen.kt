package com.noowar.smsforwarder.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noowar.smsforwarder.R
import com.noowar.smsforwarder.data.ForwardRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleListScreen(
    onAddRule: () -> Unit,
    onEditRule: (ForwardRule) -> Unit,
    onShowLog: () -> Unit,
    onShowSettings: () -> Unit,
    onShowMarquee: () -> Unit,
    viewModel: RuleViewModel = viewModel()
) {
    val rules by viewModel.rules.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_rules)) },
                actions = {
                    IconButton(onClick = onShowMarquee) {
                        Icon(Icons.Default.Tv, contentDescription = stringResource(R.string.cd_marquee))
                    }
                    IconButton(onClick = onShowLog) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.cd_forward_log))
                    }
                    IconButton(onClick = onShowSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRule) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.title_add_rule))
            }
        }
    ) { innerPadding ->
        if (rules.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.no_rules), style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.no_rules_hint), style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rules, key = { it.id }) { rule ->
                    RuleCard(
                        rule = rule,
                        onToggle = { viewModel.toggleEnabled(rule) },
                        onEdit = { onEditRule(rule) },
                        onDelete = { viewModel.deleteRule(rule) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleCard(
    rule: ForwardRule,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (rule.senderFilter.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.label_sender, rule.senderFilter),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (rule.bodyFilter.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.label_keyword, rule.bodyFilter),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (rule.senderFilter.isEmpty() && rule.bodyFilter.isEmpty()) {
                        Text(
                            text = stringResource(R.string.label_all_sms),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (rule.senderFilter.isNotEmpty() && rule.bodyFilter.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.card_label_match, rule.matchMode),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "→ ${rule.destination.ifEmpty { stringResource(R.string.label_destination_empty) }}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (rule.messageFormat) {
                            "ORIGINAL" -> stringResource(R.string.format_original)
                            "PREPEND"  -> stringResource(R.string.format_prepend)
                            "APPEND"   -> stringResource(R.string.format_append)
                            "REPLACE"  -> stringResource(R.string.format_replace)
                            else       -> stringResource(R.string.format_original)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = rule.isEnabled, onCheckedChange = { onToggle() })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.cd_edit))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cd_delete))
                }
            }
        }
    }
}
