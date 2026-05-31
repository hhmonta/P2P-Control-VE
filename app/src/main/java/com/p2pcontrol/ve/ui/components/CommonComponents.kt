package com.p2pcontrol.ve.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p2pcontrol.ve.ui.theme.*
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String = "",
    valueColor: Color = TextPrimary,
    icon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = valueColor,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
            if (icon != null) {
                icon()
            }
        }
    }
}

@Composable
fun BancoSaldoCard(
    bancoNombre: String,
    moneda: String,
    saldo: BigDecimal,
    saldoUsd: BigDecimal,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = bancoNombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${moneda} ${formatBigDecimal(saldo)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (moneda == "USD") UsdColor else VesColor
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$ ${formatBigDecimal(saldoUsd)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = UsdColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "en USD",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
fun TransaccionListItem(
    tipo: String,
    plataforma: String,
    banco: String,
    montoFiat: String,
    cantidadUsdt: String,
    gananciaPerdida: BigDecimal,
    fecha: String,
    monedaFiat: String,
    modifier: Modifier = Modifier
) {
    val isVenta = tipo == "Venta"
    val gpColor = if (gananciaPerdida >= BigDecimal.ZERO) GreenLight else RedLight
    val gpSign = if (gananciaPerdida >= BigDecimal.ZERO) "+" else ""
    val tipoColor = if (isVenta) GreenLight else OrangeAccent

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tipo,
                        style = MaterialTheme.typography.labelLarge,
                        color = tipoColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = plataforma,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$monedaFiat $montoFiat → $cantidadUsdt USDT",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = "$banco · $fecha",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
            if (isVenta) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$gpSign${formatBigDecimal(gananciaPerdida)} USDT",
                        style = MaterialTheme.typography.titleMedium,
                        color = gpColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (gananciaPerdida >= BigDecimal.ZERO) "Ganancia" else "Pérdida",
                        style = MaterialTheme.typography.labelSmall,
                        color = gpColor
                    )
                }
            }
        }
    }
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    suffix: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        prefix = if (prefix != null) {{ Text(prefix) }} else null,
        suffix = if (suffix != null) {{ Text(suffix) }} else null,
        isError = isError,
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenLight,
            unfocusedBorderColor = DarkBorder,
            focusedLabelColor = GreenLight,
            cursorColor = GreenLight,
            focusedContainerColor = DarkSurface,
            unfocusedContainerColor = DarkSurface
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = GreenLight,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

fun formatBigDecimal(value: BigDecimal): String {
    return value.setScale(2, RoundingMode.HALF_UP).toPlainString()
}
