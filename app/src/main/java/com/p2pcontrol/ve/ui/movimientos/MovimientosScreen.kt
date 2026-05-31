package com.p2pcontrol.ve.ui.movimientos

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.p2pcontrol.ve.P2PControlApp
import com.p2pcontrol.ve.data.model.TipoMovimiento
import com.p2pcontrol.ve.ui.components.*
import com.p2pcontrol.ve.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientosScreen(
    viewModel: MovimientosViewModel = viewModel(
        factory = MovimientosViewModel.Factory(
            P2PControlApp.instance.movimientoBancarioRepository,
            P2PControlApp.instance.bancoRepository,
            P2PControlApp.instance.configuracionRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Movimientos", "Ingreso", "Egreso", "Transferencia")

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            viewModel.clearSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movimientos Bancarios", fontWeight = FontWeight.Bold) },
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
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = GreenLight,
                edgePadding = 0.dp,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) GreenLight else TextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    // Lista de movimientos
                    if (uiState.movimientos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Sin movimientos registrados", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.movimientos) { mov ->
                                val isIngreso = mov.tipo == TipoMovimiento.INGRESO || mov.tipo == TipoMovimiento.TRANSFERENCIA_ENTRADA
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            if (isIngreso) GreenLight.copy(alpha = 0.3f) else RedLight.copy(alpha = 0.3f),
                                            RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    if (isIngreso) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                                                    contentDescription = null,
                                                    tint = if (isIngreso) GreenLight else RedLight,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    mov.tipo.etiqueta,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = if (isIngreso) GreenLight else RedLight,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                mov.descripcion,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                            Text(
                                                mov.fecha.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextTertiary
                                            )
                                        }
                                        Text(
                                            "${if (isIngreso) "+" else "-"}${mov.moneda.simbolo}${formatBigDecimal(mov.monto)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isIngreso) GreenLight else RedLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Formulario Ingreso
                    MovimientoForm(
                        uiState = uiState,
                        bancos = uiState.bancos,
                        onBancoChange = viewModel::setBancoSeleccionado,
                        onMontoChange = viewModel::setMonto,
                        onDescripcionChange = viewModel::setDescripcion,
                        onGuardar = viewModel::guardarIngreso,
                        label = "Registrar Ingreso"
                    )
                }
                2 -> {
                    // Formulario Egreso
                    MovimientoForm(
                        uiState = uiState,
                        bancos = uiState.bancos,
                        onBancoChange = viewModel::setBancoSeleccionado,
                        onMontoChange = viewModel::setMonto,
                        onDescripcionChange = viewModel::setDescripcion,
                        onGuardar = viewModel::guardarEgreso,
                        label = "Registrar Egreso"
                    )
                }
                3 -> {
                    // Formulario Transferencia
                    TransferenciaForm(
                        uiState = uiState,
                        bancos = uiState.bancos,
                        onBancoOrigenChange = viewModel::setBancoOrigen,
                        onBancoDestinoChange = viewModel::setBancoDestino,
                        onMontoChange = viewModel::setMonto,
                        onDescripcionChange = viewModel::setDescripcion,
                        onGuardar = viewModel::guardarTransferencia
                    )
                }
            }
        }
    }
}

@Composable
private fun MovimientoForm(
    uiState: MovimientosUiState,
    bancos: List<com.p2pcontrol.ve.data.local.entity.BancoEntity>,
    onBancoChange: (com.p2pcontrol.ve.data.local.entity.BancoEntity) -> Unit,
    onMontoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onGuardar: () -> Unit,
    label: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Banco selector
        var bancoExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = bancoExpanded,
            onExpandedChange = { bancoExpanded = it }
        ) {
            OutlinedTextField(
                value = uiState.bancoSeleccionado?.nombre ?: "Seleccionar banco",
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
                bancos.forEach { banco ->
                    DropdownMenuItem(
                        text = { Text("${banco.nombre} (${banco.moneda.simbolo})") },
                        onClick = {
                            onBancoChange(banco)
                            bancoExpanded = false
                        }
                    )
                }
            }
        }

        FormTextField(
            value = uiState.monto,
            onValueChange = onMontoChange,
            label = "Monto"
        )

        FormTextField(
            value = uiState.descripcion,
            onValueChange = onDescripcionChange,
            label = "Descripción"
        )

        Button(
            onClick = onGuardar,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenLight),
            shape = RoundedCornerShape(12.dp),
            enabled = uiState.monto.isNotEmpty()
        ) {
            Text(label, color = Green800, fontWeight = FontWeight.Bold)
        }

        if (uiState.error != null) {
            Text(uiState.error!!, color = RedLight, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TransferenciaForm(
    uiState: MovimientosUiState,
    bancos: List<com.p2pcontrol.ve.data.local.entity.BancoEntity>,
    onBancoOrigenChange: (com.p2pcontrol.ve.data.local.entity.BancoEntity) -> Unit,
    onBancoDestinoChange: (com.p2pcontrol.ve.data.local.entity.BancoEntity) -> Unit,
    onMontoChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Banco Origen
        var origenExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = origenExpanded,
            onExpandedChange = { origenExpanded = it }
        ) {
            OutlinedTextField(
                value = uiState.bancoOrigen?.nombre ?: "Seleccionar origen",
                onValueChange = {},
                readOnly = true,
                label = { Text("Banco Origen") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = origenExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedLight,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = origenExpanded,
                onDismissRequest = { origenExpanded = false }
            ) {
                bancos.forEach { banco ->
                    DropdownMenuItem(
                        text = { Text("${banco.nombre} (${banco.moneda.simbolo})") },
                        onClick = {
                            onBancoOrigenChange(banco)
                            origenExpanded = false
                        }
                    )
                }
            }
        }

        Icon(
            Icons.Filled.South,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Banco Destino
        var destinoExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = destinoExpanded,
            onExpandedChange = { destinoExpanded = it }
        ) {
            OutlinedTextField(
                value = uiState.bancoDestino?.nombre ?: "Seleccionar destino",
                onValueChange = {},
                readOnly = true,
                label = { Text("Banco Destino") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destinoExpanded) },
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
                expanded = destinoExpanded,
                onDismissRequest = { destinoExpanded = false }
            ) {
                bancos.forEach { banco ->
                    DropdownMenuItem(
                        text = { Text("${banco.nombre} (${banco.moneda.simbolo})") },
                        onClick = {
                            onBancoDestinoChange(banco)
                            destinoExpanded = false
                        }
                    )
                }
            }
        }

        FormTextField(
            value = uiState.monto,
            onValueChange = onMontoChange,
            label = "Monto a transferir"
        )

        FormTextField(
            value = uiState.descripcion,
            onValueChange = onDescripcionChange,
            label = "Descripción"
        )

        Button(
            onClick = onGuardar,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
            shape = RoundedCornerShape(12.dp),
            enabled = uiState.monto.isNotEmpty()
        ) {
            Text("TRANSFERIR", color = DarkBackground, fontWeight = FontWeight.Bold)
        }

        if (uiState.error != null) {
            Text(uiState.error!!, color = RedLight, style = MaterialTheme.typography.bodySmall)
        }
    }
}
