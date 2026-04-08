package com.example.general_first_aid_kit.presentation.component

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

@Composable
fun DeleteKitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Удалить аптечку?", color = TextBlack) },
        text = { Text(
            text = "Вы уверены, что хотите полностью удалить аптечку? Все данные о лекарствах будут потеряны для всех участников.",
            color = TextBlack
        ) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Удалить", color = TextRed)
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
fun LeaveKitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Покинуть аптечку?", color = TextBlack) },
        text = { Text(
            text = "Вы больше не сможете просматривать содержимое этой аптечки. Чтобы вернуться, вам понадобится новый код приглашения.",
            color = TextBlack
        ) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Покинуть", color = TextRed)
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