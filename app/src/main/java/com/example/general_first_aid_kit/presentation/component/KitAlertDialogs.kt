package com.example.general_first_aid_kit.presentation.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextRed
import com.example.general_first_aid_kit.presentation.ui.theme.White

@Composable
fun ChangeToPublicDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_make_public_title), color = TextBlack) },
        text = { Text(stringResource(R.string.dialog_make_public_text)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text(stringResource(R.string.action_make_public), color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = TextGray)
            }
        },
        containerColor = White
    )
}

@Composable
fun ChangeToPersonalDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_make_personal_title), color = TextBlack) },
        text = { Text(stringResource(R.string.dialog_make_personal_text)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text(stringResource(R.string.action_make_personal), color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = TextGray)
            }
        },
        containerColor = White
    )
}

@Composable
fun DeleteKitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.dialog_delete_kit_title), color = TextBlack) },
        text = {
            Text(
                text = stringResource(R.string.dialog_delete_kit_text),
                color = TextBlack
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete), color = TextRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = TextGray)
            }
        },
        containerColor = White
    )
}

@Composable
fun LeaveKitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.dialog_leave_kit_title), color = TextBlack) },
        text = {
            Text(
                text = stringResource(R.string.dialog_leave_kit_text),
                color = TextBlack
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.action_leave), color = TextRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = TextGray)
            }
        },
        containerColor = White
    )
}

@Composable
fun ArchiveKitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.kit_archive_dialog_title), color = TextBlack) },
        text = {
            Text(
                text = stringResource(R.string.kit_archive_dialog_text),
                color = TextBlack
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text(stringResource(R.string.action_archive), color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = TextGray)
            }
        },
        containerColor = White
    )
}

@Composable
fun UnarchiveKitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.kit_unarchive_dialog_title), color = TextBlack) },
        text = {
            Text(
                text = stringResource(R.string.kit_unarchive_dialog_text),
                color = TextBlack
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text(stringResource(R.string.action_activate), color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = TextGray)
            }
        },
        containerColor = White
    )
}
