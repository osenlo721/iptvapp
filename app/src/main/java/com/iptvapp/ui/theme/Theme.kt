package com.iptvapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary            = NetflixRed,
    onPrimary          = OnDark,
    primaryContainer   = NetflixRedDark,
    onPrimaryContainer = OnDark,
    secondary          = OnDarkMuted,
    onSecondary        = BackgroundDark,
    background         = BackgroundDark,
    onBackground       = OnDark,
    surface            = SurfaceDark,
    onSurface          = OnDark,
    surfaceVariant     = SurfaceCard,
    onSurfaceVariant   = OnDarkMuted,
    error              = LiveRed,
    onError            = OnDark,
    outline            = SurfaceElevated,
    outlineVariant     = Color(0xFF3D3D3D)
)

@Composable
fun IPTVAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography  = IPTVTypography,
        content     = content
    )
}
