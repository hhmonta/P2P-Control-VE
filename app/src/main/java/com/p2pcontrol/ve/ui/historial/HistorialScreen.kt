package com.p2pcontrol.ve.ui.historial

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.p2pcontrol.ve.P2PControlApp
import com.p2pcontrol.ve.data.model.TipoTransaccion
import com.p2pcontrol.ve.ui.components.*
import com.p2pcontrol.ve.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel = viewModel(
        factory = HistorialViewModel.Factory(
            P2PControlApp.instance.transaccionRepository,
            P2PControlApp.instance.bancoRepository,
            P2PControlApp.instance.plataformaRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }
    var filtroTipoExpanded by remember { mutableStateOf(false) }
    var filtroBancoExpanded by remember { mutableStateOf(false) }
    var filtroPlataformaExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = "Filtros",
                            tint = if (showFilters) GreenLight else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Filtros
            if (showFilters) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GreenLight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Filtros", style = MaterialTheme.typography.labelLarge, color = GreenLight)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Filtro tipo
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = uiState.filtroTipo == null,
                                onClick = { viewModel.setFiltroTipo(null) },
                                label = { Text("Todas") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenLight.copy(alpha = 0.2f),
                                    selectedLabelColor = GreenLight
                                )
                            )
                            FilterChip(
                                selected = uiState.filtroTipo == TipoTransaccion.COMPRA,
                                onClick = { viewModel.setFiltroTipo(TipoTransaccion.COMPRA) },
                                label = { Text("Compras") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangeAccent.copy(alpha = 0.2f),
                                    selectedLabelColor = OrangeAccent
                                )
                            )
                            FilterChip(
                                selected = uiState.filtroTipo == TipoTransaccion.VENTA,
                                onClick = { viewModel.setFiltroTipo(TipoTransaccion.VENTA) },
                                label = { Text("Ventas") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenLight.copy(alpha = 0.2f),
                                    selectedLabelColor = GreenLight
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Filtro Banco
                        var bancoExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = bancoExpanded,
                            onExpandedChange = { bancoExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.bancos.find { it.id == uiState.filtroBancoId }?.nombre ?: "Todos los bancos",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Banco") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bancoExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GreenLight,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = bancoExpanded,
                                onDismissRequest = { bancoExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos") },
                                    onClick = {
                                        viewModel.setFiltroBanco(null)
                                        bancoExpanded = false
                                    }
                                )
                                uiState.bancos.forEach { banco ->
                                    DropdownMenuItem(
                                        text = { Text(banco.nombre) },
                                        onClick = {
                                            viewModel.setFiltroBanco(banco.id)
                                            bancoExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Lista de transacciones
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenLight)
                }
            } else if (uiState.transacciones.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sin transacciones", color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
                        Text("Registra tu primera operación", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.transacciones) { tx ->
                        TransaccionListItem(
                            tipo = tx.tipo.etiqueta,
                            plataforma = "ID:${tx.plataformaId}",
                            banco = "ID:${tx.bancoId}",
                            montoFiat = formatBigDecimal(tx.montoFiat),
                            cantidadUsdt = formatBigDecimal(tx.cantidadUsdt),
                            gananciaPerdida = tx.gananciaPerdidaUsdt,
                            fecha = tx.fechaHora.toLocalDate().toString(),
                            monedaFiat = tx.fiatMoneda.simbolo
                        )
                    }
                }
            }
        }
    }
}
