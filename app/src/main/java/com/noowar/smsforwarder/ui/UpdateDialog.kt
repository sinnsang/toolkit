package com.noowar.smsforwarder.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.noowar.smsforwarder.R

@Composable
fun ForcedUpdateDialog(onUpdate: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},   // empty — cannot be dismissed by back/outside tap
        title = { Text(stringResource(R.string.update_forced_title)) },
        text = { Text(stringResource(R.string.update_forced_body)) },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text(stringResource(R.string.btn_update_now))
            }
        },
        dismissButton = null     // no dismiss button
    )
}

@Composable
fun NoTelephonyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.no_telephony_title)) },
        text = { Text(stringResource(R.string.no_telephony_body)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_ok))
            }
        },
        dismissButton = null
    )
}

@Composable
fun OptionalUpdateDialog(onUpdate: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_optional_title)) },
        text = { Text(stringResource(R.string.update_optional_body)) },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text(stringResource(R.string.btn_update_now))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_later))
            }
        }
    )
}
