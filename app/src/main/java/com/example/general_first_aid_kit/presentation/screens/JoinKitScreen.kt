package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.component.GreenSemiCircle
import com.example.general_first_aid_kit.presentation.ui.theme.Dimensions
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.LightGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.TextRed
import com.example.general_first_aid_kit.presentation.ui.theme.White
import com.example.general_first_aid_kit.presentation.viewmodels.JoinKitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinKitScreen(
    onNavigateBack: () -> Unit,
    viewModel: JoinKitViewModel = hiltViewModel()
) {
    var code by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Присоединиться", color = GreenPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(R.drawable.baseline_arrow_back_ios_24), null, tint = GreenPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            GreenSemiCircle(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.SemiCircleHeightDefault)
                    .align(Alignment.BottomCenter)
            )

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Введите 8-значный код приглашения, чтобы получить доступ к общей аптечке",
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                CodeInputView(
                    code = code,
                    onCodeChange = { code = it }
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (state.error != null) {
                    Text(
                        text = state.error!!,
                        color = TextRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Button(
                    onClick = { viewModel.joinKit(code, onSuccess = onNavigateBack) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = code.length == 8 && !state.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        disabledContainerColor = LightGray
                    ),
                    shape = RoundedCornerShape(Dimensions.CornerRadiusMedium)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Присоединиться", fontWeight = FontWeight.Bold, color = White)
                    }
                }
            }
        }
    }
}

@Composable
fun CodeInputView(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = code,
        onValueChange = {
            if (it.length <= 8) {
                onCodeChange(it.uppercase())
            }
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        decorationBox = {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            ) {
                repeat(8) { index ->
                    val char = code.getOrNull(index)?.toString() ?: ""
                    val isFocused = code.length == index
                    
                    Box(
                        modifier = Modifier
                            .size(width = 34.dp, height = 48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(White)
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) GreenPrimary else LightGray,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextBlack
                        )

                        if (isFocused) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 6.dp)
                                    .width(12.dp)
                                    .height(2.dp)
                                    .background(GreenPrimary)
                            )
                        }
                    }
                }
            }
        }
    )
}
