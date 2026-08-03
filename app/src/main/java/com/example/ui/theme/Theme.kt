package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PhoneGreenSecondary,
    secondary = PhoneGreenLight,
    tertiary = CallOutgoingBlue
)

private val LightColorScheme = lightColorScheme(
    primary = PhoneGreenPrimary,
    secondary = PhoneGreenSecondary,
    tertiary = CallOutgoingBlue,
    background = Color(0xFFFAFAFA),
    surface = Color.White
)

@Composable
fun PhoneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
