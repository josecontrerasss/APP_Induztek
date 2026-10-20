package com.example.app_induztek.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// =====================================================================
//  Theme.kt  —  TEMA GLOBAL INDUZTEK
// =====================================================================

// Esquema por defecto para el Técnico de Terreno
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
    medium = RoundedCornerShape(12.dp), // Esquinas a 12dp como indica tu mockup
    large = RoundedCornerShape(20.dp)
)

@Composable
fun MiRegistroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Para asegurar coherencia visual corporativa, forzamos el tema claro
    // y aplicamos los colores del perfil del Técnico.
    MaterialTheme(
        colorScheme = EsquemaTecnico,
        typography = Tipografia, // Mantén tu archivo Type.kt original
        shapes = Formas,
        content = content
    )
}