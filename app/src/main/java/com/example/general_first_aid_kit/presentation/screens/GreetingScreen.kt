package com.example.general_first_aid_kit.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.theme.Dimensions
import com.example.general_first_aid_kit.presentation.theme.GreenAccent
import com.example.general_first_aid_kit.presentation.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.theme.TextGreen
import com.example.general_first_aid_kit.presentation.theme.TextWhite
import com.example.general_first_aid_kit.presentation.theme.White

@Composable
fun GreetingScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenPrimary)
    ) {
        Image(
            painter = painterResource(R.drawable.square_welcome_screen_image),
            contentDescription = stringResource(R.string.image_description),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GreenPrimary.copy(alpha = 0f),
                                GreenPrimary
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GreenPrimary)
                    .padding(Dimensions.PaddingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingMedium)
            ) {
                Text(
                    text = stringResource(R.string.welcome_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = White
                )

                Text(
                    text = stringResource(R.string.welcome_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))

                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.LargeButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White,
                        contentColor = TextGreen
                    )
                ) {
                    Text(
                        text = stringResource(R.string.btn_login)
                    )
                }

                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimensions.LargeButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenAccent,
                        contentColor = TextWhite
                    )
                ) {
                    Text(
                        text = stringResource(R.string.btn_create_account)
                    )
                }

                Spacer(modifier = Modifier.height(Dimensions.SpacingMedium))
            }
        }
    }
}