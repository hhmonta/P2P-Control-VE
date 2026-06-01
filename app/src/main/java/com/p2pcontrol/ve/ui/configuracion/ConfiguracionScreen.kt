package com.p2pcontrol.ve.ui.configuracion

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.p2pcontrol.ve.P2PControlApp
import com.p2pcontrol.ve.data.model.Moneda
import com.p2pcontrol.ve.ui.components.*
import com.p2pcontrol.ve.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    viewModel: ConfiguracionViewModel = viewModel(
        factory = ConfiguracionViewModel.Factory(
            P2PControlApp.instance.configuracionRepository,
            P2PControlApp.instance.bancoRepository,
            P2PControlApp.instance.plataformaRepository,
            P2PControlApp.instance.transaccionRepository,
            P2PControlApp.instance.movimientoBancarioRepository,
            P2PControlApp.instance.inventarioUsdtRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            kotlinx.coroutines.delay(4000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Tasa USD/VES
            item {
                SectionHeader("Tasa de Referencia USD/VES")
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FormTextField(
                            value = uiState.tasaUsdVes,
                            onValueChange = viewModel::setTasaUsdVes,
                            label = "Tasa USD/VES",
                            prefix = "Bs."
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = viewModel::guardarTasa,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ACTUALIZAR TASA", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Administrar Bancos
            item {
                SectionHeader("Bancos")
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Lista de bancos existentes
                        uiState.bancos.forEach { banco ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${banco.nombre} (${banco.moneda.codigo})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (banco.activo) TextPrimary else TextTertiary
                                )
                                if (banco.activo) {
                                    IconButton(
                                        onClick = { viewModel.deactivateBanco(banco.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.VisibilityOff,
                                            contentDescription = "Ocultar",
                                            tint = TextTertiary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = DarkBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Agregar nuevo banco
                        Text("Agregar Banco", style = MaterialTheme.typography.labelLarge, color = GreenLight)
                        Spacer(modifier = Modifier.height(8.dp))

                        FormTextField(
                            value = uiState.nuevoBancoNombre,
                            onValueChange = viewModel::setNuevoBancoNombre,
                            label = "Nombre del banco"
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.nuevoBancoMoneda == Moneda.USD,
                                onClick = { viewModel.setNuevoBancoMoneda(Moneda.USD) },
                                label = { Text("USD") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = UsdColor.copy(alpha = 0.2f),
                                    selectedLabelColor = UsdColor
                                )
                            )
                            FilterChip(
                                selected = uiState.nuevoBancoMoneda == Moneda.VES,
                                onClick = { viewModel.setNuevoBancoMoneda(Moneda.VES) },
                                label = { Text("VES") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VesColor.copy(alpha = 0.2f),
                                    selectedLabelColor = VesColor
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FormTextField(
                            value = uiState.nuevoBancoSaldoInicial,
                            onValueChange = viewModel::setNuevoBancoSaldoInicial,
                            label = "Saldo inicial"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = viewModel::guardarBanco,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenLight),
                            shape = RoundedCornerShape(8.dp),
                            enabled = uiState.nuevoBancoNombre.isNotBlank()
                        ) {
                            Text("AGREGAR BANCO", color = Green800, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Administrar Plataformas
            item {
                SectionHeader("Plataformas P2P")
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        uiState.plataformas.forEach { plataforma ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    plataforma.nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (plataforma.activo) TextPrimary else TextTertiary
                                )
                                if (plataforma.activo) {
                                    IconButton(
                                        onClick = { viewModel.deactivatePlataforma(plataforma.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.VisibilityOff,
                                            contentDescription = "Ocultar",
                                            tint = TextTertiary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = DarkBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Agregar Plataforma", style = MaterialTheme.typography.labelLarge, color = GreenLight)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormTextField(
                                value = uiState.nuevaPlataformaNombre,
                                onValueChange = viewModel::setNuevaPlataformaNombre,
                                label = "Nombre",
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = viewModel::guardarPlataforma,
                                enabled = uiState.nuevaPlataformaNombre.isNotBlank()
                            ) {
                                Icon(
                                    Icons.Filled.AddCircle,
                                    contentDescription = "Agregar",
                                    tint = if (uiState.nuevaPlataformaNombre.isNotBlank()) GreenLight else TextTertiary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Exportación
            item {
                SectionHeader("Exportación")
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { viewModel.exportarCsv(context) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EXPORTAR CSV", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Exporta todas las transacciones a formato CSV",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }
            }

            // Mensajes
            if (uiState.message != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GreenLight.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = GreenLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(uiState.message!!, color = GreenLight, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            if (uiState.error != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = RedLight.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Error, null, tint = RedLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(uiState.error!!, color = RedLight, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Info de la app
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("P2P Control VE v1.0.0", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text("Control personal de operaciones P2P", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
            }
        }
    }
}
