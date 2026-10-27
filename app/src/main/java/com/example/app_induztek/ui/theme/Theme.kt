package com.example.app_induztek.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp


private val EsquemaTecnico = lightColorScheme(
    primary = VerdeTecnico,
    onPrimary = Blanco,
    primaryContainer = VerdeFondo,
    onPrimaryContainer = VerdeTecnicoOscuro,
    secondary = AlertaNaranja,
    onSecondary = Blanco,
    background = FondoGris,
    onBackground = TextoOscuro,
    surface = SuperficieClara,
    onSurface = TextoOscuro,
    error = ErrorRojo
)

private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun MiRegistroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = EsquemaTecnico,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}