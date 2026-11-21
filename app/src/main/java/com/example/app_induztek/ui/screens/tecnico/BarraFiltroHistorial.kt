package com.example.app_induztek.ui.screens.tecnico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app_induztek.ui.viewmodel.FiltroHistorial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraFiltroHistorial(
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    filtro: FiltroHistorial,
    onFiltroChange: (FiltroHistorial) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = busqueda,
            onValueChange = onBusquedaChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            placeholder = { Text("Buscar por número u observación") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (busqueda.isNotEmpty()) {
                    IconButton(onClick = { onBusquedaChange("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                    }
                }
            }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FiltroHistorial.entries.forEach { opcion ->
                val selected = filtro == opcion
                FilterChip(
                    selected = selected,
                    onClick = { onFiltroChange(opcion) },
                    label = { Text(etiquetaFiltro(opcion)) },
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
            }
        }
    }
}

private fun etiquetaFiltro(filtro: FiltroHistorial): String = when (filtro) {
    FiltroHistorial.TODOS -> "Todos"
    FiltroHistorial.PENDIENTES -> "Pendientes"
    FiltroHistorial.SINCRONIZADOS -> "Sincronizados"
}
