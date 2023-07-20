package com.example.material3_tests

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.material3_tests.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        var backgroundState by rememberSaveable { mutableStateOf(R.drawable.creative_abstract_mixed_red_color_painting_with_marble_liquid_effect_panorama) }
        Image(
            painter = painterResource(id = backgroundState),
            contentDescription = "Background picture for main frame",
            modifier = modifier.fillMaxSize(),
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop
        )
        Button(
            onClick = {
                backgroundState =
                    if (backgroundState == R.drawable.creative_abstract_mixed_red_color_painting_with_marble_liquid_effect_panorama) R.drawable.sung_jinwoo_solo_leveling
                    else R.drawable.creative_abstract_mixed_red_color_painting_with_marble_liquid_effect_panorama
            }
        ) {
            Text(
                text = "Hello $name!",
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AuthScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {},
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Login")
        }
    }
}

@Preview(
    showSystemUi = true, device = "spec:parent=pixel_5,orientation=landscape",
    name = "Auth Preview", group = "Test Preview"
)
@Composable
fun AuthUiPreview() {
    AppTheme {
        AuthScreen()
    }
}

@Preview(
    showSystemUi = true, device = "spec:parent=pixel_5,orientation=landscape",
    name = "Auth Night Preview", group = "Test Preview"
)
@Composable
fun AuthNightUiPreview() {
    AppTheme {
        AuthScreen()
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true, group = "App Preview", name = "Night Preview"
)
@Composable
fun GreetingNightPreview() {
    AppTheme {
        Greeting("Android")
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true, name = "Default Preview", group = "App Preview"
)
@Composable
fun GreetingDefaultPreview() {
    AppTheme {
        Greeting("Android")
    }
}