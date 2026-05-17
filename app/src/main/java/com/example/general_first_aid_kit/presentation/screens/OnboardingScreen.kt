package com.example.general_first_aid_kit.presentation.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.general_first_aid_kit.R
import com.example.general_first_aid_kit.presentation.ui.theme.GreenPrimary
import com.example.general_first_aid_kit.presentation.ui.theme.TextBlack
import com.example.general_first_aid_kit.presentation.ui.theme.TextGray
import com.example.general_first_aid_kit.presentation.ui.theme.White
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val iconRes: Int,
    val title: String,
    val subtitle: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val t1 = stringResource(R.string.onboarding_page1_title)
    val s1 = stringResource(R.string.onboarding_page1_subtitle)
    val t2 = stringResource(R.string.onboarding_page2_title)
    val s2 = stringResource(R.string.onboarding_page2_subtitle)
    val t3 = stringResource(R.string.onboarding_page3_title)
    val s3 = stringResource(R.string.onboarding_page3_subtitle)
    val t4 = stringResource(R.string.onboarding_page4_title)
    val s4 = stringResource(R.string.onboarding_page4_subtitle)
    val pages = remember(t1, s1, t2, s2, t3, s3, t4, s4) {
        listOf(
            OnboardingPageData(R.drawable.baseline_medication_24, t1, s1),
            OnboardingPageData(R.drawable.baseline_group_24, t2, s2),
            OnboardingPageData(R.drawable.baseline_camera_alt_24, t3, s3),
            OnboardingPageData(R.drawable.baseline_notifications_24, t4, s4)
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val currentPage = pagerState.currentPage
    val lastPageIndex = pages.size - 1

    fun finishOnboarding() {
        if (onNavigateBack == null) {
            context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_shown", true)
                .apply()
        }
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        OnboardingBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(page = pages[page])
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isActive = index == currentPage
                    val dotWidth by animateDpAsState(
                        targetValue = if (isActive) 24.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "dot_width"
                    )
                    Box(
                        modifier = Modifier
                            .width(dotWidth)
                            .height(8.dp)
                            .background(
                                color = if (isActive) GreenPrimary else GreenPrimary.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        if (currentPage < lastPageIndex) {
                            pagerState.animateScrollToPage(currentPage + 1)
                        } else {
                            finishOnboarding()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Crossfade(
                    targetState = currentPage == lastPageIndex,
                    label = "button_text"
                ) { isLast ->
                    Text(
                        text = if (isLast) stringResource(R.string.onboarding_start)
                        else stringResource(R.string.onboarding_next),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 12.dp)
        ) {
            if (onNavigateBack != null) {
                FilledTonalIconButton(
                    onClick = onNavigateBack,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = GreenPrimary.copy(alpha = 0.12f),
                        contentColor = GreenPrimary
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_close_24),
                        contentDescription = stringResource(R.string.onboarding_close)
                    )
                }
            } else {
                AnimatedVisibility(
                    visible = currentPage < lastPageIndex,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TextButton(onClick = ::finishOnboarding) {
                        Text(
                            text = stringResource(R.string.onboarding_skip),
                            color = GreenPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPageData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(GreenPrimary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(page.iconRes),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = GreenPrimary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextBlack,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.5
            ),
            color = TextGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OnboardingBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 90.dp, y = (-50).dp)
                .background(GreenPrimary.copy(alpha = 0.07f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-65).dp, y = 65.dp)
                .background(GreenPrimary.copy(alpha = 0.06f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopStart)
                .offset(x = (-40).dp, y = 110.dp)
                .background(GreenPrimary.copy(alpha = 0.05f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-28).dp, y = (-130).dp)
                .background(GreenPrimary.copy(alpha = 0.08f), CircleShape)
        )
    }
}
