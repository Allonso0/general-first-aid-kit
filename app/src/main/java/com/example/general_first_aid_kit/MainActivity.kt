package com.example.general_first_aid_kit

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.general_first_aid_kit.presentation.navigation.NavigationRoot
import com.example.general_first_aid_kit.presentation.ui.theme.GeneralfirstaidkitTheme
import com.example.general_first_aid_kit.presentation.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashScreen.setKeepOnScreenCondition {
            authViewModel.isLoading.value
        }

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val scaleX = ObjectAnimator.ofFloat(splashScreenView.view, View.SCALE_X, 1f, 0f)
            val scaleY = ObjectAnimator.ofFloat(splashScreenView.view, View.SCALE_Y, 1f, 0f)
            val alpha = ObjectAnimator.ofFloat(splashScreenView.view, View.ALPHA, 1f, 0f)

            val interpolator = AnticipateInterpolator()

            scaleX.interpolator = interpolator
            scaleY.interpolator = interpolator

            scaleX.duration = 600L
            scaleY.duration = 600L
            alpha.duration = 600L

            scaleX.doOnEnd { splashScreenView.remove() }

            scaleX.start()
            scaleY.start()
            alpha.start()
        }

        setContent {
            GeneralfirstaidkitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavigationRoot()
                }
            }
        }
    }
}