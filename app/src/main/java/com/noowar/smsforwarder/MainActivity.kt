package com.noowar.smsforwarder

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.noowar.smsforwarder.data.ForwardRule
import com.noowar.smsforwarder.data.MarqueeItem
import com.noowar.smsforwarder.data.VersionCheck
import com.noowar.smsforwarder.service.SmsNotificationListener
import com.noowar.smsforwarder.service.SmsWatcherService
import com.noowar.smsforwarder.ui.ForcedUpdateDialog
import com.noowar.smsforwarder.ui.ForwardLogScreen
import com.noowar.smsforwarder.ui.MarqueeDisplayScreen
import com.noowar.smsforwarder.ui.MarqueeListScreen
import com.noowar.smsforwarder.ui.NoTelephonyDialog
import com.noowar.smsforwarder.ui.OptionalUpdateDialog
import com.noowar.smsforwarder.ui.RuleEditScreen
import com.noowar.smsforwarder.ui.RuleListScreen
import com.noowar.smsforwarder.ui.SettingsScreen
import com.noowar.smsforwarder.ui.theme.SmsForwarderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmsForwarderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

private sealed class UpdatePrompt {
    data class Forced(val url: String) : UpdatePrompt()
    data class Optional(val url: String) : UpdatePrompt()
}

internal class NavState : ViewModel() {
    var isEditing by mutableStateOf(false)
    var editingRule by mutableStateOf<ForwardRule?>(null)
    var isShowingLog by mutableStateOf(false)
    var isShowingSettings by mutableStateOf(false)
    var isShowingMarquee by mutableStateOf(false)
    var displayingMarqueeItem by mutableStateOf<MarqueeItem?>(null)
}

@Composable
fun AppContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var updatePrompt by remember { mutableStateOf<UpdatePrompt?>(null) }
    val hasTelephony = remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }
    var noTelephonyDismissed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val info = VersionCheck.fetch() ?: return@LaunchedEffect
        @Suppress("DEPRECATION")
        val currentCode = context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        updatePrompt = when {
            currentCode < info.minVersionCode -> UpdatePrompt.Forced(info.updateUrl)
            currentCode < info.latestVersionCode -> UpdatePrompt.Optional(info.updateUrl)
            else -> null
        }
    }

    when (val prompt = updatePrompt) {
        is UpdatePrompt.Forced -> ForcedUpdateDialog(
            onUpdate = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(prompt.url))) }
        )
        is UpdatePrompt.Optional -> OptionalUpdateDialog(
            onUpdate = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(prompt.url))) },
            onDismiss = { updatePrompt = null }
        )
        null -> {
            MainUI(modifier = modifier, context = context, lifecycleOwner = lifecycleOwner)
            if (!hasTelephony && !noTelephonyDismissed) {
                NoTelephonyDialog(onDismiss = { noTelephonyDismissed = true })
            }
        }
    }
}

@Composable
private fun MainUI(modifier: Modifier, context: Context, lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
    val smsPermissions = buildList {
        add(Manifest.permission.READ_SMS)
        add(Manifest.permission.RECEIVE_SMS)
        add(Manifest.permission.SEND_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    var permissionsGranted by remember {
        mutableStateOf(smsPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        })
    }
    var notificationAccess by remember { mutableStateOf(isNotificationAccessGranted(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationAccess = isNotificationAccessGranted(context)
                if (notificationAccess && permissionsGranted) {
                    context.startForegroundService(Intent(context, SmsWatcherService::class.java))
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        permissionsGranted = result.values.all { it }
        if (permissionsGranted && notificationAccess) {
            context.startForegroundService(Intent(context, SmsWatcherService::class.java))
        }
    }

    when {
        !permissionsGranted -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.permission_sms_required))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permLauncher.launch(smsPermissions) }) {
                    Text(stringResource(R.string.btn_grant_permission))
                }
            }
        }
        !notificationAccess -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.notification_access_required))
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.notification_access_guide))
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }) {
                    Text(stringResource(R.string.btn_grant_notification_access))
                }
            }
        }
        else -> {
            val nav: NavState = viewModel()
            val activity = context as? android.app.Activity
            var showExitDialog by remember { mutableStateOf(false) }

            BackHandler {
                when {
                    nav.displayingMarqueeItem != null -> nav.displayingMarqueeItem = null
                    nav.isShowingMarquee -> nav.isShowingMarquee = false
                    nav.isShowingSettings -> nav.isShowingSettings = false
                    nav.isShowingLog -> nav.isShowingLog = false
                    nav.isEditing -> nav.isEditing = false
                    else -> showExitDialog = true
                }
            }

            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text(stringResource(R.string.exit_dialog_title)) },
                    text = { Text(stringResource(R.string.exit_dialog_body)) },
                    confirmButton = {
                        TextButton(onClick = { activity?.finish() }) {
                            Text(stringResource(R.string.exit_dialog_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExitDialog = false }) {
                            Text(stringResource(R.string.exit_dialog_cancel))
                        }
                    }
                )
            }

            when {
                nav.displayingMarqueeItem != null -> {
                    MarqueeDisplayScreen(
                        item = nav.displayingMarqueeItem!!,
                        onBack = { nav.displayingMarqueeItem = null }
                    )
                }
                nav.isShowingMarquee -> {
                    MarqueeListScreen(
                        onBack = { nav.isShowingMarquee = false },
                        onDisplay = { item -> nav.displayingMarqueeItem = item }
                    )
                }
                nav.isShowingSettings -> {
                    SettingsScreen(onBack = { nav.isShowingSettings = false })
                }
                nav.isShowingLog -> {
                    ForwardLogScreen(onBack = { nav.isShowingLog = false })
                }
                nav.isEditing -> {
                    RuleEditScreen(
                        initial = nav.editingRule,
                        onBack = { nav.isEditing = false }
                    )
                }
                else -> {
                    RuleListScreen(
                        onAddRule = {
                            nav.editingRule = null
                            nav.isEditing = true
                        },
                        onEditRule = { rule ->
                            nav.editingRule = rule
                            nav.isEditing = true
                        },
                        onShowLog = { nav.isShowingLog = true },
                        onShowSettings = { nav.isShowingSettings = true },
                        onShowMarquee = { nav.isShowingMarquee = true }
                    )
                }
            }
        }
    }
}

private fun isNotificationAccessGranted(context: Context): Boolean {
    val cn = ComponentName(context, SmsNotificationListener::class.java)
    val flat = Settings.Secure.getString(
        context.contentResolver, "enabled_notification_listeners"
    ) ?: return false
    return flat.split(":").mapNotNull { ComponentName.unflattenFromString(it) }.any { it == cn }
}
