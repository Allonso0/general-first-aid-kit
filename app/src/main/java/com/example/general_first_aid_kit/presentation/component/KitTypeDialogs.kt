package com.example.general_first_aid_kit.presentation.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.White

@Composable
fun ChangeToPublicDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сделать аптечку общей?", color = TextBlack) },
        text = {
            Text("После смены типа вы сможете приглашать других пользователей. Они смогут просматривать и редактировать список лекарств.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Сделать общей", color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextGray)
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
        title = { Text("Сделать аптечку личной?", color = TextBlack) },
        text = {
            Text("Все текущие участники будут немедленно удалены. Доступ к аптечке останется только у вас.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Сделать личной", color = White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextGray)
            }
        },
        containerColor = White
    )
}