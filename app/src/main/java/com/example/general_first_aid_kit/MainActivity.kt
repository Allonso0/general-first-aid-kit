package com.example.general_first_aid_kit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.general_first_aid_kit.presentation.navigation.NavigationRoot
import com.example.general_first_aid_kit.presentation.theme.GeneralfirstaidkitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GeneralfirstaidkitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavigationRoot()
                }
            }
        }
    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    GeneralfirstaidkitTheme {
//        Greeting("Android")
//    }
//}