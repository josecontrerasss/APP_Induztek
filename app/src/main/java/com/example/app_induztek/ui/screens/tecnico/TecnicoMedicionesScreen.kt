package com.example.app_induztek.ui.screens.tecnico

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.ui.components.BotonPrincipalInduztek
import com.example.app_induztek.ui.theme.*
import com.example.app_induztek.ui.viewmodel.InduztekViewModel
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TecnicoMedicionScreen(
    viewModel: InduztekViewModel,
    onLogout: () -> Unit
) {
    val contexto = LocalContext.current
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val instalaciones by viewModel.todasLasInstalaciones.collectAsStateWithLifecycle()
    val equipos by viewModel.todosLosEquipos.collectAsStateWithLifecycle()
    val pendientes by viewModel.pendientesSyncCount.collectAsStateWithLifecycle()
    val selInstalacion by viewModel.selectedInstalacionId.collectAsStateWithLifecycle()
    val selEquipo by viewModel.selectedEquipoId.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (pendientes > 0) Sincronizacion.copy(alpha = 0.15f) else VerdeFondo
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (pendientes > 0) "$pendientes mediciones por sincronizar" else "SQLite local sincronizado ✔",
                    color = if (pendientes > 0) AlertaNaranja else ExitoVerde,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (pendientes > 0) {
                    TextButton(onClick = { viewModel.sincronizarProtocolos() }) {
                        Text("Sincronizar ahora", color = TextoOscuro)
                    }
                }
            }
        }

        Text("Subestación y Equipo:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextoOscuro)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            instalaciones.forEach { inst ->
                FilterChip(
                    selected = selInstalacion == inst.id_instalacion,
                    onClick = { viewModel.selectInstalacion(inst.id_instalacion) },
                    label = { Text(inst.codigo) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            equipos.filter { it.id_instalacion == selInstalacion }.forEach { eq ->
                FilterChip(
                    selected = selEquipo == eq.id_equipo,
                    onClick = { viewModel.selectEquipo(eq.id_equipo) },
                    label = { Text(eq.tag) }
                )
            }
        }

        Text("Mediciones:", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = TextoOscuro)

        CampoMedicion("Tensión Primario", form.tensionPrimario, "V", keyboardType = KeyboardType.Number) { viewModel.onTensionPrimarioChange(it) }
        CampoMedicion("Tensión Secundario", form.tensionSecundario, "V", keyboardType = KeyboardType.Number) { viewModel.onTensionSecundarioChange(it) }
        CampoMedicion("Corriente Medida", form.corriente, "A", keyboardType = KeyboardType.Decimal) { viewModel.onCorrienteChange(it) }
        CampoMedicion("Resistencia Aislamiento", form.resistenciaAislamiento, "MΩ", keyboardType = KeyboardType.Text) { viewModel.onResistenciaChange(it) }

        CampoMedicion("Temperatura Aceite", form.temperaturaAceite, "°C", esAlerta = form.isTemperaturaAlerta, keyboardType = KeyboardType.Decimal) {
            viewModel.onTemperaturaChange(it)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Observaciones:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextoOscuro)
            Text("${form.observaciones.length}/200", fontSize = 12.sp, color = TextoSecundario)
        }
        OutlinedTextField(
            value = form.observaciones,
            onValueChange = { viewModel.onObservacionesChange(it) },
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(8.dp)
        )

        BotonUbicacionMedicion(
            onUbicacionCapturada = { lat, lon -> viewModel.onUbicacionCapturada(lat, lon) }
        )

        BotonPrincipalInduztek(
            texto = "GUARDAR MEDICIÓN EN TERRENO",
            onClick = { viewModel.guardarMedicion() }
        )
    }
}

@Composable
private fun CampoMedicion(
    label: String,
    valor: String,
    unidad: String,
    esAlerta: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Number,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { nuevoValor ->
            val filtrado = when (keyboardType) {
                KeyboardType.Number -> nuevoValor.filter { it.isDigit() }
                KeyboardType.Decimal -> nuevoValor.filter { it.isDigit() || it == '.' }
                else -> nuevoValor
            }
            onCambio(filtrado)
        },
        label = { Text(label, color = TextoSecundario) },
        suffix = { Text(unidad, fontWeight = FontWeight.Bold, color = TextoOscuro) },
        trailingIcon = {
            Icon(
                imageVector = if (esAlerta) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (esAlerta) ErrorRojo else ExitoVerde
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun BotonUbicacionMedicion(onUbicacionCapturada: (String, String) -> Unit) {
    val contexto = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(contexto) }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { ubicacion ->
                    ubicacion?.let {
                        latitud = it.latitude.toString()
                        longitud = it.longitude.toString()
                        onUbicacionCapturada(latitud, longitud)
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { ubicacion ->
                        ubicacion?.let {
                            latitud = it.latitude.toString()
                            longitud = it.longitude.toString()
                            onUbicacionCapturada(latitud, longitud)
                        }
                    }
                } else {
                    permissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeTecnico,
                contentColor = Blanco
            )
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "GPS")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Validar ubicación en terreno")
        }

        if (latitud.isNotEmpty() && longitud.isNotEmpty()) {
            Text(
                text = "📍 Coordenadas: $latitud, $longitud",
                color = TextoOscuro,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
            )

            val latLng = com.google.android.gms.maps.model.LatLng(latitud.toDouble(), longitud.toDouble())
            val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
                position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(latLng, 15f)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 8.dp)
            ) {
                com.google.maps.android.compose.GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    com.google.maps.android.compose.Marker(
                        state = com.google.maps.android.compose.MarkerState(position = latLng),
                        title = "Ubicación del Técnico"
                    )
                }
            }
        }
    }
}