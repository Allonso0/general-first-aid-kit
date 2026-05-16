package com.example.general_first_aid_kit

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.animation.AccelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.general_first_aid_kit.presentation.navigation.NavigationRoot
import com.example.general_first_aid_kit.presentation.ui.theme.GeneralfirstaidkitTheme
import com.example.general_first_aid_kit.presentation.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            GeneralfirstaidkitTheme(darkTheme = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavigationRoot()
                }
            }
        }
    }
}