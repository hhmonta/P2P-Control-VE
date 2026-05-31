package com.p2pcontrol.ve.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.p2pcontrol.ve.P2PControlApp
import com.p2pcontrol.ve.ui.components.*
import com.p2pcontrol.ve.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNuevaTransaccion: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(
            P2PControlApp.instance.bancoRepository,
            P2PControlApp.instance.transaccionRepository,
            P2PControlApp.instance.movimientoBancarioRepository,
            P2PControlApp.instance.configuracionRepository,
            P2PControlApp.instance.inventarioUsdtRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "P2P Control VE",
                        fontWeight = FontWeight.Bold,
                        color = GreenLight
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevaTransaccion,
                containerColor = GreenLight,
                contentColor = Green800
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva transacción")
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenLight)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Tarjetas de resumen
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryCard(
                            title = "Balance Total",
                            value = "$ ${formatBigDecimal(uiState.balanceTotalUsd)}",
                            valueColor = UsdColor,
                            icon = {
                                Icon(
                                    Icons.Filled.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = UsdColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryCard(
                            title = "USDT Disponibles",
                            value = formatBigDecimal(uiState.usdtDisponibles),
                            valueColor = UsdtColor,
                            icon = {
                                Icon(
                                    Icons.Filled.CurrencyBitcoin,
                                    contentDescription = null,
                                    tint = UsdtColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "G/P del Mes",
                            value = "${if (uiState.gananciaPerdidaMes >= java.math.BigDecimal.ZERO) "+" else ""}${formatBigDecimal(uiState.gananciaPerdidaMes)}",
                            valueColor = if (uiState.gananciaPerdidaMes >= java.math.BigDecimal.ZERO) GreenLight else RedLight,
                            icon = {
                                Icon(
                                    if (uiState.gananciaPerdidaMes >= java.math.BigDecimal.ZERO) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (uiState.gananciaPerdidaMes >= java.math.BigDecimal.ZERO) GreenLight else RedLight,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Tasa de referencia
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Tasa USD/VES",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Text(
                                "Bs. ${formatBigDecimal(uiState.tasaUsdVes)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Saldos por banco
                item {
                    SectionHeader(title = "Saldos por Banco")
                }

                items(uiState.bancosConSaldo) { bancoSaldo ->
                    BancoSaldoCard(
                        bancoNombre = bancoSaldo.banco.nombre,
                        moneda = bancoSaldo.banco.moneda.simbolo,
                        saldo = bancoSaldo.saldoActual,
                        saldoUsd = bancoSaldo.saldoEnUsd
                    )
                }

                // Últimas transacciones
                item {
                    SectionHeader(title = "Últimas Transacciones")
                }

                items(uiState.ultimasTransacciones) { tx ->
                    TransaccionListItem(
                        tipo = tx.tipo.etiqueta,
                        plataforma = "", // Se resolvería con un join
                        banco = "",
                        montoFiat = formatBigDecimal(tx.montoFiat),
                        cantidadUsdt = formatBigDecimal(tx.cantidadUsdt),
                        gananciaPerdida = tx.gananciaPerdidaUsdt,
                        fecha = tx.fechaHora.toLocalDate().toString(),
                        monedaFiat = tx.fiatMoneda.simbolo
                    )
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
