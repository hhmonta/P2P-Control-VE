package com.p2pcontrol.ve.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Panel", Icons.Filled.Dashboard)
    data object NuevaTransaccion : Screen("nueva_transaccion", "Nuevo", Icons.Filled.AddCircle)
    data object Historial : Screen("historial", "Historial", Icons.Filled.History)
    data object Movimientos : Screen("movimientos", "Movimientos", Icons.Filled.AccountBalance)
    data object Configuracion : Screen("configuracion", "Ajustes", Icons.Filled.Settings)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.NuevaTransaccion,
    Screen.Historial,
    Screen.Movimientos,
    Screen.Configuracion
)
