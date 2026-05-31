package com.p2pcontrol.ve.ui.transaccion

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.p2pcontrol.ve.P2PControlApp
import com.p2pcontrol.ve.data.model.Moneda
import com.p2pcontrol.ve.data.model.TipoTransaccion
import com.p2pcontrol.ve.ui.components.*
import com.p2pcontrol.ve.ui.theme.*
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaTransaccionScreen(
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {},
    viewModel: TransaccionViewModel = viewModel(
        factory = TransaccionViewModel.Factory(
            P2PControlApp.instance.transaccionRepository,
            P2PControlApp.instance.bancoRepository,
            P2PControlApp.instance.plataformaRepository,
            P2PControlApp.instance.movimientoBancarioRepository,
            P2PControlApp.instance.inventarioUsdtRepository,
            P2PControlApp.instance.configuracionRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Transacción", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Tabs Compra / Venta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = uiState.tipo == TipoTransaccion.COMPRA,
                    onClick = { viewModel.setTipo(TipoTransaccion.COMPRA) },
                    label = { Text("COMPRA", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangeAccent.copy(alpha = 0.3f),
                        selectedLabelColor = OrangeAccent
                    )
                )
                FilterChip(
                    selected = uiState.tipo == TipoTransaccion.VENTA,
                    onClick = { viewModel.setTipo(TipoTransaccion.VENTA) },
                    label = { Text("VENTA", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GreenLight.copy(alpha = 0.3f),
                        selectedLabelColor = GreenLight
                    )
                )
            }

            // Descripción del tipo
            Text(
                text = if (uiState.tipo == TipoTransaccion.COMPRA)
                    "Recibes USDT, entregas fiat"
                else
                    "Entregas USDT, recibes fiat",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            // Plataforma
            var plataformaExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = plataformaExpanded,
                onExpandedChange = { plataformaExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.plataformaSeleccionada?.nombre ?: "Seleccionar",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Plataforma") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = plataformaExpanded) },
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
                    expanded = plataformaExpanded,
                    onDismissRequest = { plataformaExpanded = false }
                ) {
                    uiState.plataformas.forEach { plataforma ->
                        DropdownMenuItem(
                            text = { Text(plataforma.nombre) },
                            onClick = {
                                viewModel.setPlataforma(plataforma)
                                plataformaExpanded = false
                            }
                        )
                    }
                }
            }

            // Banco
            var bancoExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = bancoExpanded,
                onExpandedChange = { bancoExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.bancoSeleccionado?.nombre ?: "Seleccionar",
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
                    uiState.bancos.forEach { banco ->
                        DropdownMenuItem(
                            text = { Text("${banco.nombre} (${banco.moneda.simbolo})") },
                            onClick = {
                                viewModel.setBanco(banco)
                                bancoExpanded = false
                            }
                        )
                    }
                }
            }

            // Moneda Fiat (auto-detectada del banco)
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Moneda Fiat:", color = TextSecondary)
                    Text(
                        uiState.fiatMoneda.codigo,
                        color = if (uiState.fiatMoneda == Moneda.USD) UsdColor else VesColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Monto Fiat
            FormTextField(
                value = uiState.montoFiat,
                onValueChange = viewModel::setMontoFiat,
                label = "Monto Fiat",
                prefix = uiState.fiatMoneda.simbolo
            )

            // Cantidad USDT
            FormTextField(
                value = uiState.cantidadUsdt,
                onValueChange = viewModel::setCantidadUsdt,
                label = "Cantidad USDT",
                suffix = "USDT"
            )

            // Tasa calculada
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tasa implícita:", color = TextSecondary)
                    Text(
                        if (uiState.tasa.isNotEmpty()) "${uiState.tasa} ${uiState.fiatMoneda.codigo}/USDT" else "—",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Comisión
            FormTextField(
                value = uiState.comisionUsdt,
                onValueChange = viewModel::setComision,
                label = "Comisión (USDT)",
                suffix = "USDT"
            )

            // Ganancia/Pérdida (solo ventas)
            if (uiState.tipo == TipoTransaccion.VENTA && uiState.gananciaPerdidaCalculada != BigDecimal.ZERO) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.gananciaPerdidaCalculada >= BigDecimal.ZERO)
                            GreenLight.copy(alpha = 0.15f)
                        else
                            RedLight.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (uiState.gananciaPerdidaCalculada >= BigDecimal.ZERO) "Ganancia" else "Pérdida",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (uiState.gananciaPerdidaCalculada >= BigDecimal.ZERO) GreenLight else RedLight
                        )
                        Text(
                            "${if (uiState.gananciaPerdidaCalculada >= BigDecimal.ZERO) "+" else ""}${formatBigDecimal(uiState.gananciaPerdidaCalculada)} USDT",
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (uiState.gananciaPerdidaCalculada >= BigDecimal.ZERO) GreenLight else RedLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Notas
            FormTextField(
                value = uiState.notas,
                onValueChange = viewModel::setNotas,
                label = "Notas (opcional)",
                singleLine = false
            )

            // Botón Guardar
            Button(
                onClick = viewModel::guardarTransaccion,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenLight),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isSaving && uiState.montoFiat.isNotEmpty() && uiState.cantidadUsdt.isNotEmpty()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Green800
                    )
                } else {
                    Text(
                        "GUARDAR TRANSACCIÓN",
                        color = Green800,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Error
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = RedLight,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
